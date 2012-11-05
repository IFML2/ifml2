package ifml2.players.guiplayer;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.engine.Engine;
import ifml2.engine.EngineVersion;
import ifml2.interfaces.GUIInterface;
import ifml2.om.IFML2LoadXmlException;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.ValidationEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ListIterator;

public class GUIPlayer extends JFrame
{
    private JPanel mainPanel;
    private JTextField commandText;
    private JTextArea logTextArea;

    private GUIInterface guiInterface;
    private Engine engine;

    private final ArrayList<String> commandHistory = new ArrayList<String>();
    private ListIterator<String> historyIterator = commandHistory.listIterator();
    private String storyFile;
    private static final Logger LOG = Logger.getLogger(GUIPlayer.class);

    private void initEngine()
    {
        guiInterface = new GUIInterface(logTextArea, commandText);
        engine = new Engine(guiInterface);
    }

    private static String getStoryFileNameForPlay(String[] args)
    {
        String storyFile;

        // option #1 -- the first argument is file name 
        if(args != null && args.length >= 1)
        {
            // first parameter is story file name
            storyFile = args[0];
            if(new File(storyFile).exists())
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

        if(ifmlFileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }
        storyFile = ifmlFileChooser.getSelectedFile().getAbsolutePath();

        return storyFile;
    }

    public static void main(String[] args)
    {
        GUIPlayer guiPlayer = new GUIPlayer();

        final String storyFile = getStoryFileNameForPlay(args);
        if(storyFile == null)
        {
            JOptionPane.showMessageDialog(null, "История не выбрана, Плеер завершает свою работу");
            guiPlayer.dispose();
            return;
        }

        guiPlayer.setVisible(true);

        guiPlayer.loadStory(storyFile);
    }

    private void loadStory(String storyFile)
    {
        this.storyFile = storyFile;

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
        if(exception instanceof IFML2LoadXmlException)
        {
            guiInterface.outputText("\nВ файле истории есть ошибки:");
            for(ValidationEvent validationEvent : ((IFML2LoadXmlException)exception).getEvents())
            {
                guiInterface.outputText("\n\"{0}\" at {1},{2}", validationEvent.getMessage(),
                        validationEvent.getLocator().getLineNumber(), validationEvent.getLocator().getColumnNumber());
            }
        }
        else
        {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            guiInterface.outputText("\nПроизошла ошибка: {0}", stringWriter.toString());
        }
    }

    private GUIPlayer()
    {
        initEngine();

        setTitle("ЯРИЛ 2.0 " + EngineVersion.IFML_ENGINE_VERSION);
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
                    try
                    {
                        String gamerCommand = guiInterface.inputText();
                        updateHistory(gamerCommand);
                        
                        if("заново!".equalsIgnoreCase(gamerCommand))
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
//                        logTextArea.append(MessageFormat.format("Системная ошибка: {0}\n{1}", ex.getMessage(), Arrays.toString(ex.getStackTrace())));
//                        ex.printStackTrace();
                    }
                }

                else
                // history prev callback
                if(KeyEvent.VK_UP == key || KeyEvent.VK_KP_UP == key)
                {
                    commandText.setText(getHistoryPrev());
                }

                else
                // history next callback
                if(KeyEvent.VK_DOWN == key || KeyEvent.VK_KP_DOWN == key)
                {
                    commandText.setText(getHistoryNext());
                }
            }
        });

        commandText.requestFocus();
    }

    private String getHistoryNext()
    {
        if(historyIterator.hasNext())
        {
            return historyIterator.next();
        }
        else
        {
            return "";
        }
    }

    private String getHistoryPrev()
    {
        if(historyIterator.hasPrevious())
        {
            return historyIterator.previous();
        }
        else
        {
            return "";
        }
    }

    private void updateHistory(String gamerCommand)
    {
        commandHistory.add(gamerCommand);
        historyIterator = commandHistory.listIterator(commandHistory.size());
    }
}
