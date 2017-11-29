package ifml2.players.guiplayer;

import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.engine.featureproviders.graphic.IOutputIconProvider;
import ifml2.om.IFML2LoadXmlException;
import ifml2.engine.featureproviders.text.IOutputPlainTextProvider;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.bind.ValidationEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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

public class GUIPlayer extends JFrame implements IOutputPlainTextProvider, IOutputIconProvider {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GUIPlayer.class);
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

    private GUIPlayer(boolean fromTempFile) {
        super(format("%s Плеер %s", RUSSIAN_PRODUCT_NAME, VERSION));
        this.isFromTempFile = fromTempFile;

        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setJMenuBar(createMainMenu());

        GUIUtils.packAndCenterWindow(this);

        commandText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // command entry
                if (KeyEvent.VK_ENTER == key) {
                    // test if just enter + scroll bar isn't at the bottom
                    BoundedRangeModel scrollModel = scrollPane.getVerticalScrollBar().getModel();
                    int value = scrollModel.getValue();
                    int extent = scrollModel.getExtent();
                    boolean isNotAtTheBottom = value + extent < scrollModel.getMaximum();
                    if ("".equals(commandText.getText().trim()) && isNotAtTheBottom) {
                        scrollModel.setValue(value + extent); // scroll to next page
                    } else {
                        processCommand(getCommandText());
                    }
                } else
                // history prev callback
                if (KeyEvent.VK_UP == key || KeyEvent.VK_KP_UP == key) {
                    commandText.setText(goHistoryPrev());
                } else
                // history next callback
                if (KeyEvent.VK_DOWN == key || KeyEvent.VK_KP_DOWN == key) {
                    commandText.setText(goHistoryNext());
                }
            }
        });

        commandText.requestFocusInWindow();
        setVisible(true);
    }

    private static String acquireStoryFileNameForPlay(String[] args) {
        String storyFile;

        // option #1 -- the first argument is file name
        if (args != null && args.length >= 1) {
            // first parameter is story file name
            storyFile = args[0];
            if (new File(storyFile).exists()) {
                return storyFile;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Файл истории \"" + storyFile + "\" не найден.\n" + "Файл будет выбран вручную.",
                        "Файл не найден", JOptionPane.ERROR_MESSAGE);
            }
        }

        // option #2 -- show open file dialog
        return showOpenStoryFileDialog(null);
    }

    private static String showOpenStoryFileDialog(Window owner) {
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getGamesDirectory());
        storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files
                                                                                               // filter
        storyFileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return STORY_ALL_TYPES_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(STORY_EXTENSION)
                        || f.getName().toLowerCase().endsWith(CIPHERED_STORY_EXTENSION);
            }
        });

        storyFileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                return f.isDirectory() ? DIRECTORY_ICON : STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
            return storyFileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> startFromFile(acquireStoryFileNameForPlay(args), false));
    }

    public static void startFromFile(String fileName, boolean isFromTempFile) {
        if (fileName != null) {
            GUIPlayer player = new GUIPlayer(isFromTempFile);
            player.loadStory(fileName);
        } else {
            JOptionPane.showMessageDialog(null, "История не выбрана, Плеер завершает свою работу");
        }
    }

    private void processCommand(String gamerCommand) {
        echoCommand(gamerCommand);

        if ("".equals(gamerCommand.trim())) {
            outputPlainText("Введите что-нибудь.\n");
            return;
        }

        updateHistory(gamerCommand);

        try {
            if (START_ANEW_COMMAND.equalsIgnoreCase(gamerCommand)) {
                startAnew();
                return;
            }

            if (SAVE_COMMAND.equalsIgnoreCase(gamerCommand)) {
                saveGame();
                return;
            }

            if (LOAD_COMMAND.equalsIgnoreCase(gamerCommand)) {
                loadGame();
                return;
            }
        } catch (IFML2Exception ex) {
            reportError(ex, "Ошибка при перезапуске истории!");
        }

        engine.executeGamerCommand(gamerCommand);
    }

    private void loadGame() {
        JFileChooser savedGameFileChooser = new JFileChooser(CommonUtils.getSavesDirectory());
        savedGameFileChooser.removeChoosableFileFilter(savedGameFileChooser.getAcceptAllFileFilter()); // remove All
                                                                                                       // files filter
        savedGameFileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return SAVE_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(SAVE_EXTENSION);
            }
        });

        savedGameFileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                return f.isDirectory() ? DIRECTORY_ICON : SAVE_FILE_ICON;
            }
        });

        if (savedGameFileChooser.showOpenDialog(GUIPlayer.this) == JFileChooser.APPROVE_OPTION) {
            String saveFileName = savedGameFileChooser.getSelectedFile().getAbsolutePath();
            try {
                if (new File(saveFileName).exists()) {
                    engine.loadGame(saveFileName);
                } else {
                    outputPlainText("Выбранный файл не существует.");
                }
            } catch (IFML2Exception ex) {
                GUIUtils.showErrorMessage(GUIPlayer.this, ex);
            }
        } else {
            outputPlainText("Загрузка отменена.\n");
        }
    }

    private void saveGame() {
        JFileChooser savedGameFileChooser = new JFileChooser(CommonUtils.getSavesDirectory());
        savedGameFileChooser.removeChoosableFileFilter(savedGameFileChooser.getAcceptAllFileFilter()); // remove All
                                                                                                       // files filter
        savedGameFileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return SAVE_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(SAVE_EXTENSION);
            }
        });

        savedGameFileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                return f.isDirectory() ? DIRECTORY_ICON : SAVE_FILE_ICON;
            }
        });

        if (savedGameFileChooser.showSaveDialog(GUIPlayer.this) == JFileChooser.APPROVE_OPTION) {
            String saveFileName = savedGameFileChooser.getSelectedFile().getAbsolutePath();
            if (!saveFileName.toLowerCase().endsWith(SAVE_EXTENSION)) {
                saveFileName += SAVE_EXTENSION;
            }
            try {
                engine.saveGame(saveFileName);
            } catch (IFML2Exception ex) {
                GUIUtils.showErrorMessage(GUIPlayer.this, ex);
            }
        } else {
            outputPlainText("Сохранение отменено.\n");
        }
    }

    private void startAnew() throws IFML2Exception {
        outputPlainText("Начинаем заново...\n");
        engine.loadStory(storyFile, true);
        engine.initGame();
    }

    private JMenuBar createMainMenu() {
        JMenuBar mainMenu = new JMenuBar();

        JMenu storyMenu = new JMenu("История");
        storyMenu.add(new AbstractAction("Начать новую историю...", NEW_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(GUIPlayer.this,
                        "Вы действительно хотите завершить текущую историю и начать новую?\r\n"
                                + "Ведь всё, чего Вы тут достигли - не сохранится.",
                        "Новая история", YES_NO_OPTION, QUESTION_MESSAGE);
                if (answer == YES_OPTION) {
                    String fileName = showOpenStoryFileDialog(GUIPlayer.this);
                    if (fileName != null) {
                        loadStory(fileName);
                    }
                }

                focusCommandText();
            }
        });
        storyMenu.add(new AbstractAction("Начать сначала...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(GUIPlayer.this,
                        "Вы действительно хотите начать историю заново?\r\n"
                                + "Ведь всё, чего Вы тут достигли - не сохранится.",
                        "Начать заново", YES_NO_OPTION, QUESTION_MESSAGE);
                if (answer == YES_OPTION) {
                    processCommand(START_ANEW_COMMAND);
                }

                focusCommandText();
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Сохранить игру...", SAVE_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                processCommand(SAVE_COMMAND);
            }
        });
        storyMenu.add(new AbstractAction("Загрузить игру...", OPEN_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                processCommand(LOAD_COMMAND);
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Выйти...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(GUIPlayer.this,
                        "Вы действительно хотите выйти?\r\n" + "Ведь всё, чего Вы тут достигли - не сохранится.",
                        "Выйти", YES_NO_OPTION, QUESTION_MESSAGE);
                if (answer == YES_OPTION) {
                    GUIPlayer.this.dispose();
                } else {
                    focusCommandText();
                }
            }
        });
        mainMenu.add(storyMenu);
        return mainMenu;
    }

    private void focusCommandText() {
        SwingUtilities.invokeLater(() -> commandText.requestFocus());
    }

    private void loadStory(String storyFile) {
        setStoryFile(storyFile);

        // load story
        try {
            logTextPane.setText("Загрузка...");
            engine.loadStory(this.storyFile, true);
            logTextPane.setText("");
            engine.initGame();

            SwingUtilities.invokeLater(() -> {
                // move scrollBar to the top...
                scrollPane.getVerticalScrollBar().setValue(0);
            });
        } catch (Throwable e) {
            reportError(e, "Ошибка при загрузке истории!");
        }
    }

    private void reportError(Throwable exception, String message) {
        exception.printStackTrace();
        LOG.error(message, exception);

        if (exception instanceof IFML2LoadXmlException) {
            outputPlainText("\nВ файле истории есть ошибки:");
            for (ValidationEvent validationEvent : ((IFML2LoadXmlException) exception).getEvents()) {
                outputPlainText(MessageFormat.format("\n\"{0}\" at {1},{2}", validationEvent.getMessage(),
                        validationEvent.getLocator().getLineNumber(), validationEvent.getLocator().getColumnNumber()));
            }
        } else {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            outputPlainText(MessageFormat.format("\nПроизошла ошибка: {0}", stringWriter.toString()));
        }
    }

    private String goHistoryNext() {
        return historyIterator.hasNext() ? historyIterator.next() : "";
    }

    private String goHistoryPrev() {
        return historyIterator.hasPrevious() ? historyIterator.previous() : "";
    }

    private void updateHistory(String gamerCommand) {
        commandHistory.add(gamerCommand);
        historyIterator = commandHistory.listIterator(commandHistory.size());
    }

    public void setStoryFile(String storyFile) {
        this.storyFile = storyFile;
        updateTitle();
    }

    private void updateTitle() {
        String titleFile = "";
        if (isFromTempFile) {
            titleFile = "запущен из Редактора";
        } else {
            File file = new File(storyFile);
            if (file.exists()) {
                titleFile = file.getName();
            }
        }
        setTitle(format("%s Плеер %s -- %s", RUSSIAN_PRODUCT_NAME, VERSION, titleFile));
    }

    public String getCommandText() {
        String command = commandText.getText();
        commandText.setText("");
        return command;
    }

    private void echoCommand(String command) {
        outputPlainText("\n");

        // prepare for scrolling to command
        Rectangle startLocation;
        try {
            startLocation = logTextPane.modelToView(logTextPane.getStyledDocument().getLength());
        } catch (BadLocationException e) {
            LOG.error("Error while scrolling JTextArea", e);
            throw new RuntimeException(e);
        }

        // echo command
        outputPlainText("> " + command + "\n");

        // scroll to inputted command
        final Point viewPosition = new Point(startLocation.x, startLocation.y);
        SwingUtilities.invokeLater(() -> {
            if (scrollPane.getVerticalScrollBar().isShowing()) {
                JViewport viewPort = scrollPane.getViewport();
                viewPort.setEnabled(false); // disable viewPort to avoid flickering
                try {
                    viewPort.setViewPosition(viewPosition);
                } finally {
                    viewPort.setEnabled(true); // enable to repaint
                }
            }
        });
    }

    @Override
    public void outputPlainText(String text) {
        StyledDocument document = logTextPane.getStyledDocument();
        try {
            document.insertString(document.getLength(), text, null);
        } catch (BadLocationException e) {
            LOG.error("Error while inserting string to JTextPane", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void outputIcon(Icon icon) {
        logTextPane.setCaretPosition(logTextPane.getStyledDocument().getLength());
        logTextPane.insertIcon(icon);
    }
}
