package ifml2;

import ifml2.editor.gui.Editor;
import ifml2.players.guiplayer.GUIPlayer;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Launcher
{
    private static final Logger LOG = Logger.getLogger(Launcher.class);

    /**
     * @param args if first arg is 'player' or 'editor' then appropriate program will be launched, else player is launched with first parameter
     */
    public static void main(String[] args)
    {
        // log system properties
        StringWriter stringWriter = new StringWriter();
        System.getProperties().list(new PrintWriter(stringWriter));
        LOG.debug(String.format("System properties:\n%s", stringWriter.toString()));

        if(args.length > 0 && "player".equalsIgnoreCase(args[0]))
        {
            GUIPlayer.main(getOtherArgs(args));
        }
        else
        {
            if(args.length > 0 && "editor".equalsIgnoreCase(args[0]))
            {
                Editor.main(getOtherArgs(args));
            }
            else
            {
                GUIPlayer.main(args);
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
