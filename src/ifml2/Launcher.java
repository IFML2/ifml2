package ifml2;

import ifml2.editor.gui.Editor;
import ifml2.players.guiplayer.GUIPlayer;
import ifml2.tests.gui.TestRunner;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Map;

import static ifml2.CommonConstants.RUSSIAN_PRODUCT_NAME;
import static java.lang.String.format;

public class Launcher
{
    private static final Logger LOG = Logger.getLogger(Launcher.class);

    /**
     * @param args if first arg is 'player' or 'editor' then appropriate program will be launched, else player is launched with first parameter
     */
    public static void main(String[] args)
    {
        LOG.debug(MessageFormat.format("Launcher.main(args = \"{0}\")", (Object[]) args));

        LOG.debug("System properties:");
        for(Map.Entry<Object, Object> entry : System.getProperties().entrySet())
        {
            LOG.debug(format("%s = %s", entry.getKey(), entry.getValue()));
        }

        if(args.length > 0 && "player".equalsIgnoreCase(args[0]))
        {
            GUIPlayer.main(getOtherArgs(args));
        }
        else if(args.length > 0 && "editor".equalsIgnoreCase(args[0]))
        {
            Editor.main(getOtherArgs(args));
        }
        else if(args.length > 0 && "tester".equalsIgnoreCase(args[0]))
        {
            TestRunner.main(getOtherArgs(args));
        }
        else
        {
            Object playerOption = new Object()
            {
                @Override
                public String toString()
                {
                    return "Плеер";
                }
            };
            Object editorOption = new Object()
            {
                @Override
                public String toString()
                {
                    return "Редактор";
                }
            };
            Object testerOption = new Object()
            {
                @Override
                public String toString()
                {
                    return "Тестер";
                }
            };
            Object answer = JOptionPane.showInputDialog(null, "Что запустить?", format("%s %s", RUSSIAN_PRODUCT_NAME, CommonUtils.getVersion()),
                    JOptionPane.QUESTION_MESSAGE, null, new Object[]{playerOption, editorOption, testerOption}, playerOption);
            if(playerOption.equals(answer))
            {
                GUIPlayer.main(args);
            }
            else if(editorOption.equals(answer))
            {
                Editor.main(args);
            }
            else if(testerOption.equals(answer))
            {
                TestRunner.main(args);
            }
        }
    }

    private static String[] getOtherArgs(String[] args)
    {
        if(args == null || args.length <= 1)
        {
            return new String[]{};
        }
        else
        {
            String[] result = new String[args.length - 1];
            System.arraycopy(args, 1, result, 0, args.length - 1);
            return result;
        }
    }
}
