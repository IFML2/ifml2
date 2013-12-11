package ifml2.players.guiplayer;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.IFML2LoadXmlException;
import ifml2.players.GameInterface;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.BadLocationException;
import javax.xml.bind.ValidationEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ListIterator;

public class GUIPlayer extends JFrame
{
    private static final Logger LOG = Logger.getLogger(GUIPlayer.class);
    private static final String START_ANEW_COMMAND = "заново!";
    private static final String SAVE_COMMAND = "сохранить";
    private static final String LOAD_COMMAND = "загрузить";
    private final ArrayList<String> commandHistory = new ArrayList<String>();
    private JPanel mainPanel;
    private JTextField commandText;
    private JTextArea logTextArea;
    private JScrollPane scrollPane;
    private GUIPlayerGameInterface gameInterface = new GUIPlayerGameInterface();
    private Engine engine = new Engine(gameInterface);
    private ListIterator<String> historyIterator = commandHistory.listIterator();
    private String storyFile;
    private boolean isFromTempFile;

    private GUIPlayer(boolean fromTempFile)
    {
        super("ЯРИЛ 2.0 Плеер " + Engine.ENGINE_VERSION);
        this.isFromTempFile = fromTempFile;

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                if (isFromTempFile)
                {
                    // delete temp file
                    File tempFile = new File(storyFile);
                    if (!tempFile.delete())
                    {
                        LOG.error(MessageFormat.format("Can't delete temp file {0}", tempFile.getAbsolutePath()));
                    }
                }
            }
        });

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
                        processCommand(gameInterface.inputText());
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

    private void processCommand(String gamerCommand)
    {
        gameInterface.echoCommand(gamerCommand);

        if ("".equals(gamerCommand.trim()))
        {
            gameInterface.outputText("Введите что-нибудь.\n");
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

            if(SAVE_COMMAND.equalsIgnoreCase(gamerCommand))
            {
                saveGame();
                return;
            }

            if(LOAD_COMMAND.equalsIgnoreCase(gamerCommand))
            {
                loadGame();
                return;
            }
        }
        catch (IFML2Exception ex)
        {
            ReportError(ex, "Ошибка при перезапуске истории!");
        }

        engine.executeGamerCommand(gamerCommand);
    }

    private void loadGame()
    {
        JFileChooser savedGameFileChooser = new JFileChooser(CommonUtils.getSavesDirectory());
        savedGameFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return CommonConstants.SAVE_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(CommonConstants.SAVE_EXTENSION);
            }
        });

        savedGameFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                if(f.isDirectory())
                {
                    return GUIUtils.DIRECTORY_ICON;
                }
                return GUIUtils.SAVE_FILE_ICON;
            }
        });

        if (savedGameFileChooser.showOpenDialog(GUIPlayer.this) == JFileChooser.APPROVE_OPTION)
        {
            String saveFileName = savedGameFileChooser.getSelectedFile().getAbsolutePath();
            try
            {
                if(new File(saveFileName).exists())
                {
                    engine.loadGame(saveFileName);
                }
                else
                {
                    gameInterface.outputText("Выбранный файл не существует.");
                }
            }
            catch (IFML2Exception ex)
            {
                GUIUtils.showErrorMessage(GUIPlayer.this, ex);
            }
        }
        else
        {
            gameInterface.outputText("Загрузка отменена.\n");
        }
    }

    private void saveGame()
    {
        JFileChooser savedGameFileChooser = new JFileChooser(CommonUtils.getSavesDirectory());
        savedGameFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return CommonConstants.SAVE_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(CommonConstants.SAVE_EXTENSION);
            }
        });

        savedGameFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                if(f.isDirectory())
                {
                    return GUIUtils.DIRECTORY_ICON;
                }
                return GUIUtils.SAVE_FILE_ICON;
            }
        });


        if (savedGameFileChooser.showSaveDialog(GUIPlayer.this) == JFileChooser.APPROVE_OPTION)
        {
            String saveFileName = savedGameFileChooser.getSelectedFile().getAbsolutePath();
            if (!saveFileName.toLowerCase().endsWith(CommonConstants.SAVE_EXTENSION))
            {
                saveFileName += CommonConstants.SAVE_EXTENSION;
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
            gameInterface.outputText("Сохранение отменено.\n");
        }
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
                                                    "Файл будет выбран вручную.", "Файл не найден",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        // option #2 -- show open file dialog
        return showOpenStoryFileDialog(null);
    }

    private static String showOpenStoryFileDialog(Window owner)
    {
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
        storyFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return CommonConstants.STORY_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(CommonConstants.STORY_EXTENSION);
            }
        });

        storyFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                if (f.isDirectory())
                {
                    return GUIUtils.DIRECTORY_ICON;
                }
                return GUIUtils.STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION)
        {
            return storyFileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    public static void main(String[] args)
    {
        startFromFile(acquireStoryFileNameForPlay(args), false);
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

    private void startAnew() throws IFML2Exception
    {
        gameInterface.outputText("Начинаем заново...\n");
        engine.loadStory(storyFile);
        engine.initGame();
    }

    private JMenuBar createMainMenu()
    {
        JMenuBar mainMenu = new JMenuBar();

        JMenu storyMenu = new JMenu("История");
        storyMenu.add(new AbstractAction("Начать новую историю...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (JOptionPane.showConfirmDialog(GUIPlayer.this,
                                                  "Вы действительно хотите завершить текущую историю и начать новую?\r\n" +
                                                  "Ведь всё, чего Вы тут достигли - не сохранится.", "Новая история",
                                                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
                    JOptionPane.YES_OPTION)
                {
                    String fileName = showOpenStoryFileDialog(GUIPlayer.this);
                    if (fileName != null)
                    {
                        loadStory(fileName);
                    }
                }
            }
        });
        storyMenu.add(new AbstractAction("Начать сначала...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (JOptionPane.showConfirmDialog(GUIPlayer.this, "Вы действительно хотите начать историю заново?\r\n" +
                                                                  "Ведь всё, чего Вы тут достигли - не сохранится.",
                                                  "Начать заново", JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    //startAnew();
                    processCommand(START_ANEW_COMMAND);
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Сохранить игру...", GUIUtils.SAVE_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                processCommand(SAVE_COMMAND);
            }
        });
        storyMenu.add(new AbstractAction("Загрузить игру...", GUIUtils.OPEN_ICON)
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
                if (JOptionPane.showConfirmDialog(GUIPlayer.this, "Вы действительно хотите выйти?\r\n" +
                                                                  "Ведь всё, чего Вы тут достигли - не сохранится.",
                                                  "Выйти", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
                    JOptionPane.YES_OPTION)
                {
                    GUIPlayer.this.dispose();
                }
            }
        });
        mainMenu.add(storyMenu);
        return mainMenu;
    }

    private void loadStory(String storyFile)
    {
        setStoryFile(storyFile);

        // load story
        try
        {
            logTextArea.setText("Загрузка...");
            engine.loadStory(this.storyFile);
            logTextArea.setText("");
            engine.initGame();

            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    // move scrollBar to the top...
                    scrollPane.getVerticalScrollBar().setValue(0);
                }
            });
        }
        catch (Throwable e)
        {
            ReportError(e, "Ошибка при загрузке истории!");
        }
    }

    private void ReportError(Throwable exception, String message)
    {
        exception.printStackTrace();
        LOG.error(message, exception);
        if (exception instanceof IFML2LoadXmlException)
        {
            gameInterface.outputText("\nВ файле истории есть ошибки:");
            for (ValidationEvent validationEvent : ((IFML2LoadXmlException) exception).getEvents())
            {
                gameInterface.outputText(MessageFormat.format("\n\"{0}\" at {1},{2}", validationEvent.getMessage(),
                                                             validationEvent.getLocator().getLineNumber(),
                                                             validationEvent.getLocator().getColumnNumber()));
            }
        }
        else
        {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            gameInterface.outputText(MessageFormat.format("\nПроизошла ошибка: {0}", stringWriter.toString()));
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
        setTitle("ЯРИЛ 2.0 Плеер " + Engine.ENGINE_VERSION + " -- " + titleFile);
    }

    private class GUIPlayerGameInterface implements GameInterface
    {
        @Override
        public void outputText(String text)
        {
            logTextArea.append(text);
        }

        @Override
        public String inputText()
        {
            String command = commandText.getText();
            commandText.setText("");
            //echoCommand(command);

            return command;
        }

        private void echoCommand(String command)
        {
            outputText("\n");

            // prepare for scrolling to command
            Rectangle startLocation;
            int lastLine = logTextArea.getLineCount() - 1;
            try
            {
                startLocation = logTextArea.modelToView(logTextArea.getLineStartOffset(lastLine));
            }
            catch (BadLocationException e)
            {
                LOG.error("Error while scrolling JTextArea", e);
                throw new RuntimeException(e);
            }

            // echo command
            outputText("> " + command + "\n");

            // scroll to inputted command
            final Point viewPosition = new Point(startLocation.x, startLocation.y);
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
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
                }
            });
        }
    }
}
