package ifml2.players;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;

import java.util.Scanner;

public class ConsolePlayer
{
    /**
     * @param args First arg is story file path
     */
    public static void main(String[] args)
    {
        GameInterface textInterface = new GameInterface()
        {
            private final Scanner scanner = new Scanner(System.in);

            @Override
            public void outputText(String text)
            {
                System.out.print(text);
            }

            @Override
            public String inputText()
            {
                outputText("\n> ");
                return scanner.nextLine();
            }
        };
        Engine engine = new Engine(textInterface);

        if (args.length < 1)
        {
            System.out.println("Пропущен обязательный параметр - файл игры.");
            return;
        }

        try
        {
            engine.loadStory(args[0], true);
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
