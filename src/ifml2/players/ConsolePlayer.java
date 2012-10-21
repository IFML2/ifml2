package ifml2.players;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.interfaces.TextInterface;

public class ConsolePlayer
{
    /**
     * @param args  First arg is story file path 
     */
    public static void main(String[] args)
    {
        TextInterface textInterface = new TextInterface();
        Engine engine = new Engine(textInterface);

        if (args.length < 1)
        {
            System.out.println("Пропущен обязательный параметр - файл игры.");
            return;
        }

        try
		{
			engine.loadStory(args[0]);
            engine.initGame();
		}
		catch (IFML2Exception e)
		{
			System.out.println(e.getMessage());
			return;
		}

        String gamerCommand;
        do
        {
            gamerCommand = textInterface.inputText();
        }
        while (engine.executeGamerCommand(gamerCommand));
    }
}
