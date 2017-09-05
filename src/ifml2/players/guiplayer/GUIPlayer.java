package ifml2.players.guiplayer;

import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.engine.featureproviders.graphic.IOutputIconProvider;
import ifml2.engine.featureproviders.text.IOutputPlainTextProvider;
import ifml2.om.IFML2LoadXmlException;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.bind.ValidationEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ListIterator;

import static ifml2.CommonConstants.*;
import static ifml2.GUIUtils.*;
import static ifml2.engine.EngineVersion.VERSION;
import static java.lang.String.format;
import static javax.swing.JOptionPane.*;

public class GUIPlayer extends JFrame implements IOutputPlainTextProvider, IOutputIconProvider
{
    private static final Logger LOG = Logger.getLogger(GUIPlayer.class);
    private static final String START_ANEW_COMMAND = "заново!";
    private static final String SAVE_COMMAND = "сохранить";
    private static final String LOAD_COMMAND = "загрузить";
    private final ArrayList<String> commandHistory = new ArrayList<>();
    private JPanel mainPanel;
    private JTextField commandText;
    private JTextPane logTextPane;
    private JScrollPane scrollPane;
    private Engine engine = new Engine(this);
    private ListIterator<String> historyIterator = commandHistory.listIterator();
    private String storyFile;
    private boolean isFromTempFile;

    private GUIPlayer(boolean fromTempFile)
    {
        super(format("%s Плеер %s", RUSSIAN_PRODUCT_NAME, VERSION));
        this.isFromTempFile = fromTempFile;

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setJMenuBar(createMainMenu());

        GUIUtils.packAndCenterWindow(this);

        commandText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();

                // command entry
                if (KeyEvent.VK_ENTER == key)
                {
                    // test if just enter + scroll bar isn't at the bottom
                    BoundedRangeModel scrollModel = scrollPane.getVerticalScrollBar().getModel();
                    int value = scrollModel.getValue();
                    int extent = scrollModel.getExtent();
                    boolean isNotAtTheBottom = value + extent < scrollModel.getMaximum();
                    if ("".equals(commandText.getText().trim()) && isNotAtTheBottom)
                    {
                        scrollModel.setValue(value + extent); // scroll to next page
                    }
                    else
                    {
                        processCommand(getCommandText());
                    }
                }
                else
                    // history prev callback
                    if (KeyEvent.VK_UP == key || KeyEvent.VK_KP_UP == key)
                    {
                        commandText.setText(goHistoryPrev());
                    }
                    else
                        // history next callback
                        if (KeyEvent.VK_DOWN == key || KeyEvent.VK_KP_DOWN == key)
                        {
                            commandText.setText(goHistoryNext());
                        }
            }
        });

        commandText.requestFocusInWindow();
        setVisible(true);
    }

    private static String acquireStoryFileNameForPlay(String[] args)
    {
        String storyFile;

        // option #1 -- the first argument is file name
        if (args != null && args.length >= 1)
        {
            // first parameter is story file name
            storyFile = args[0];
            if (new File(storyFile).exists())
            {
                return storyFile;
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Файл истории \"" + storyFile + "\" не найден.\n" +
                                                    "Файл будет выбран вручную.", "Файл не найден", JOptionPane.ERROR_MESSAGE);
            }
        }

        // option #2 -- show open file dialog
        return showOpenStoryFileDialog(null);
    }

    private static String showOpenStoryFileDialog(Window owner)
    {
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getGamesDirectory());
        storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
        storyFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return STORY_ALL_TYPES_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(STORY_EXTENSION) ||
                       f.getName().toLowerCase().endsWith(CIPHERED_STORY_EXTENSION);
            }
        });

        storyFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                return f.isDirectory() ? DIRECTORY_ICON : STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION)
        {
            return storyFileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(
                () -> startFromFile(acquireStoryFileNameForPlay(args), false));
    }

    public static void startFromFile(String fileName, boolean isFromTempFile)
    {
        if (fileName != null)
        {
            GUIPlayer player = new GUIPlayer(isFromTempFile);
            player.loadStory(fileName);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "История не выбрана, Плеер завершает свою работу");
        }
    }

    private void processCommand(String gamerCommand)
    {
        echoCommand(gamerCommand);

        if ("".equals(gamerCommand.trim()))
        {
            outputPlainText("Введите что-нибудь.\n");
            return;
        }

        updateHistory(gamerCommand);

        try
        {
            if (START_ANEW_COMMAND.equalsIgnoreCase(gamerCommand))
            {
                startAnew();
                return;
            }

            if (SAVE_COMMAND.equalsIgnoreCase(gamerCommand))
            {
                saveGame();
                return;
            }

            if (LOAD_COMMAND.equalsIgnoreCase(gamerCommand))
            {
                loadGame();
                return;
            }
        }
        catch (IFML2Exception ex)
        {
            reportError(ex, "Ошибка при перезапуске истории!");
        }

        engine.executeGamerCommand(gamerCommand);
    }

    private void loadGame()
    {
        JFileChooser savedGameFileChooser = new JFileChooser(CommonUtils.getSavesDirectory());
        savedGameFileChooser.removeChoosableFileFilter(savedGameFileChooser.getAcceptAllFileFilter()); // remove All files filter
        savedGameFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return SAVE_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(SAVE_EXTENSION);
            }
        });

        savedGameFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                return f.isDirectory() ? DIRECTORY_ICON : SAVE_FILE_ICON;
            }
        });

        if (savedGameFileChooser.showOpenDialog(GUIPlayer.this) == JFileChooser.APPROVE_OPTION)
        {
            String saveFileName = savedGameFileChooser.getSelectedFile().getAbsolutePath();
            try
            {
                if (new File(saveFileName).exists())
                {
                    engine.loadGame(saveFileName);
                }
                else
                {
                    outputPlainText("Выбранный файл не существует.");
                }
            }
            catch (IFML2Exception ex)
            {
                GUIUtils.showErrorMessage(GUIPlayer.this, ex);
            }
        }
        else
        {
            outputPlainText("Загрузка отменена.\n");
        }
    }

    private void saveGame()
    {
        JFileChooser savedGameFileChooser = new JFileChooser(CommonUtils.getSavesDirectory());
        savedGameFileChooser.removeChoosableFileFilter(savedGameFileChooser.getAcceptAllFileFilter()); // remove All files filter
        savedGameFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return SAVE_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(SAVE_EXTENSION);
            }
        });

        savedGameFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                return f.isDirectory() ? DIRECTORY_ICON : SAVE_FILE_ICON;
            }
        });


        if (savedGameFileChooser.showSaveDialog(GUIPlayer.this) == JFileChooser.APPROVE_OPTION)
        {
            String saveFileName = savedGameFileChooser.getSelectedFile().getAbsolutePath();
            if (!saveFileName.toLowerCase().endsWith(SAVE_EXTENSION))
            {
                saveFileName += SAVE_EXTENSION;
            }
            try
            {
                engine.saveGame(saveFileName);
            }
            catch (IFML2Exception ex)
            {
                GUIUtils.showErrorMessage(GUIPlayer.this, ex);
            }
        }
        else
        {
            outputPlainText("Сохранение отменено.\n");
        }
    }

    private void startAnew() throws IFML2Exception
    {
        outputPlainText("Начинаем заново...\n");
        engine.loadStory(storyFile, true);
        engine.initGame();
    }

    private JMenuBar createMainMenu()
    {
        JMenuBar mainMenu = new JMenuBar();

        JMenu storyMenu = new JMenu("История");
        storyMenu.add(new AbstractAction("Начать новую историю...", NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int answer = JOptionPane.showConfirmDialog(GUIPlayer.this,
                        "Вы действительно хотите завершить текущую историю и начать новую?\r\n" +
                        "Ведь всё, чего Вы тут достигли - не сохранится.", "Новая история", YES_NO_OPTION,
                        QUESTION_MESSAGE);
                if (answer == YES_OPTION)
                {
                    String fileName = showOpenStoryFileDialog(GUIPlayer.this);
                    if (fileName != null)
                    {
                        loadStory(fileName);
                    }
                }

                focusCommandText();
            }
        });
        storyMenu.add(new AbstractAction("Начать сначала...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int answer = JOptionPane.showConfirmDialog(GUIPlayer.this,
                        "Вы действительно хотите начать историю заново?\r\n" + "Ведь всё, чего Вы тут достигли - не сохранится.",
                        "Начать заново", YES_NO_OPTION, QUESTION_MESSAGE);
                if (answer == YES_OPTION)
                {
                    processCommand(START_ANEW_COMMAND);
                }

                focusCommandText();
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Сохранить игру...", SAVE_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                processCommand(SAVE_COMMAND);
            }
        });
        storyMenu.add(new AbstractAction("Загрузить игру...", OPEN_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                processCommand(LOAD_COMMAND);
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Выйти...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int answer = JOptionPane.showConfirmDialog(GUIPlayer.this,
                        "Вы действительно хотите выйти?\r\n" + "Ведь всё, чего Вы тут достигли - не сохранится.", "Выйти",
                        YES_NO_OPTION, QUESTION_MESSAGE);
                if (answer == YES_OPTION)
                {
                    GUIPlayer.this.dispose();
                }
                else
                {
                    focusCommandText();
                }
            }
        });
        mainMenu.add(storyMenu);
        return mainMenu;
    }

    private void focusCommandText()
    {
        SwingUtilities.invokeLater(() -> commandText.requestFocus());
    }

    private void loadStory(String storyFile)
    {
        setStoryFile(storyFile);

        // load story
        try
        {
            logTextPane.setText("Загрузка...");
            engine.loadStory(this.storyFile, true);
            logTextPane.setText("");
            engine.initGame();

            SwingUtilities.invokeLater(() -> {
                // move scrollBar to the top...
                scrollPane.getVerticalScrollBar().setValue(0);
            });
        }
        catch (Throwable e)
        {
            reportError(e, "Ошибка при загрузке истории!");
        }
    }

    private void reportError(Throwable exception, String message)
    {
        exception.printStackTrace();
        LOG.error(message, exception);

            if (exception instanceof IFML2LoadXmlException)
            {
                outputPlainText("\nВ файле истории есть ошибки:");
                for (ValidationEvent validationEvent : ((IFML2LoadXmlException) exception).getEvents())
                {
                    outputPlainText(MessageFormat
                                    .format("\n\"{0}\" at {1},{2}", validationEvent.getMessage(), validationEvent.getLocator().getLineNumber(),
                                            validationEvent.getLocator().getColumnNumber()));
                }
            }
            else
            {
                StringWriter stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                outputPlainText(MessageFormat.format("\nПроизошла ошибка: {0}", stringWriter.toString()));
            }
    }

    private String goHistoryNext()
    {
        return historyIterator.hasNext() ? historyIterator.next() : "";
    }

    private String goHistoryPrev()
    {
        return historyIterator.hasPrevious() ? historyIterator.previous() : "";
    }

    private void updateHistory(String gamerCommand)
    {
        commandHistory.add(gamerCommand);
        historyIterator = commandHistory.listIterator(commandHistory.size());
    }

    public void setStoryFile(String storyFile)
    {
        this.storyFile = storyFile;
        updateTitle();
    }

    private void updateTitle()
    {
        String titleFile = "";
        if (isFromTempFile)
        {
            titleFile = "запущен из Редактора";
        }
        else
        {
            File file = new File(storyFile);
            if (file.exists())
            {
                titleFile = file.getName();
            }
        }
        setTitle(format("%s Плеер %s -- %s", RUSSIAN_PRODUCT_NAME, VERSION, titleFile));
    }

    public String getCommandText()
    {
        String command = commandText.getText();
        commandText.setText("");
        return command;
    }

    private void echoCommand(String command)
    {
        outputPlainText("\n");

        // prepare for scrolling to command
        Rectangle startLocation;
        try
        {
            startLocation = logTextPane.modelToView(logTextPane.getStyledDocument().getLength());
        }
        catch (BadLocationException e)
        {
            LOG.error("Error while scrolling JTextArea", e);
            throw new RuntimeException(e);
        }

        // echo command
        outputPlainText("> " + command + "\n");

        // scroll to inputted command
        final Point viewPosition = new Point(startLocation.x, startLocation.y);
        SwingUtilities.invokeLater(() -> {
            if (scrollPane.getVerticalScrollBar().isShowing())
            {
                JViewport viewPort = scrollPane.getViewport();
                viewPort.setEnabled(false); // disable viewPort to avoid flickering
                try
                {
                    viewPort.setViewPosition(viewPosition);
                }
                finally
                {
                    viewPort.setEnabled(true); // enable to repaint
                }
            }
        });
    }

    @Override
    public void outputPlainText(String text) {
        HTMLDocument doc = (HTMLDocument)logTextPane.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit)logTextPane.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (BadLocationException | IOException e) {
            LOG.error("Error while inserting string to JTextPane", e);
            throw new RuntimeException(e);
        }

        /*StyledDocument document = logTextPane.getStyledDocument();
        try {
            document.insertString(document.getLength(), "<html>" + text + "</html>", null);
        } catch (BadLocationException e) {
            LOG.error("Error while inserting string to JTextPane", e);
            throw new RuntimeException(e);
        }*/
    }

    @Override
    public void outputIcon(Icon icon) {
        logTextPane.setCaretPosition(logTextPane.getStyledDocument().getLength());
        logTextPane.insertIcon(icon);
        outputPlainText("<strong>HTML test</strong>\n" +
                "<img class=\"icon icons8-Танцы\" width=\"96\" height=\"96\" src=\"data:image/png;base64,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\">");
    }
}
