package ifml2.players.guiplayer;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.engine.Engine;
import ifml2.om.IFML2LoadXmlException;
import ifml2.players.GameInterface;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.xml.bind.ValidationEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ListIterator;

public class GUIPlayer extends JFrame
{
    private static final Logger LOG = Logger.getLogger(GUIPlayer.class);
    private final ArrayList<String> commandHistory = new ArrayList<String>();
    private JPanel mainPanel;
    private JTextField commandText;
    private JTextArea logTextArea;
    private JScrollPane scrollPane;
    private GameInterface guiInterface = new GameInterface()
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
                    if(scrollPane.getVerticalScrollBar().isShowing())
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

            return command;
        }
    };
    private Engine engine = new Engine(guiInterface);
    private ListIterator<String> historyIterator = commandHistory.listIterator();
    private String storyFile;
    private boolean isFromTempFile;

    private GUIPlayer(boolean fromTempFile)
    {
        super("ЯРИЛ 2.0 Плеер " + Engine.ENGINE_VERSION);
        this.isFromTempFile = fromTempFile;

        setContentPane(mainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try
                {
                    if(isFromTempFile)
                    {
                        File tempFile = new File(storyFile);
                        if (!tempFile.delete())
                        {
                            LOG.error(MessageFormat.format("Can't delete temp file {0}", tempFile.getAbsolutePath()));
                        }
                    }
                }
                finally
                {
                    dispose();
                }
            }
        });

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
                        return;
                    }

                    try
                    {
                        String gamerCommand = guiInterface.inputText();

                        if ("".equals(gamerCommand.trim()))
                        {
                            guiInterface.outputText("Введите что-нибудь.\n");
                            return;
                        }

                        updateHistory(gamerCommand);

                        if ("заново!".equalsIgnoreCase(gamerCommand))
                        {
                            guiInterface.outputText("Начинаем заново...\n");
                            engine.loadStory(storyFile);
                            engine.initGame();
                            return;
                        }

                        engine.executeGamerCommand(gamerCommand);
                    }
                    catch (Throwable ex)
                    {
                        ReportError(ex, "Ошибка при перезапуске истории!");
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
                                                    "Файл будет выбран вручную.", "Файл не найден",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        // option #2 -- show open file dialog
        JFileChooser ifmlFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
        ifmlFileChooser.setFileFilter(new FileFilter()
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

        if (ifmlFileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }
        storyFile = ifmlFileChooser.getSelectedFile().getAbsolutePath();

        return storyFile;
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
            /*if(isFromTempFile)
            {
                File tempFile = new File(fileName);
                if (!tempFile.delete())
                {
                    LOG.error(MessageFormat.format("Can't delete temp file {0}", tempFile.getAbsolutePath()));
                }
            }*/
        }
        else
        {
            JOptionPane.showMessageDialog(null, "История не выбрана, Плеер завершает свою работу");
        }
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
            guiInterface.outputText("\nВ файле истории есть ошибки:");
            for (ValidationEvent validationEvent : ((IFML2LoadXmlException) exception).getEvents())
            {
                guiInterface.outputText(MessageFormat.format("\n\"{0}\" at {1},{2}", validationEvent.getMessage(),
                                                             validationEvent.getLocator().getLineNumber(),
                                                             validationEvent.getLocator().getColumnNumber()));
            }
        }
        else
        {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            guiInterface.outputText(MessageFormat.format("\nПроизошла ошибка: {0}", stringWriter.toString()));
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
        if(isFromTempFile)
        {
            titleFile = "запущен из Редактора";
        }
        else
        {
            File file = new File(storyFile);
            if(file.exists())
            {
                titleFile = file.getName();
            }
        }
        setTitle("ЯРИЛ 2.0 Плеер " + Engine.ENGINE_VERSION + " -- " + titleFile);
    }
}
