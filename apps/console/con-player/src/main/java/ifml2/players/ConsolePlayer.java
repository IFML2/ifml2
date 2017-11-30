package ifml2.players;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.engine.featureproviders.text.IOutputPlainTextProvider;

import java.util.Scanner;

public class ConsolePlayer {
    /**
     * @param args First arg is story file path
     */
    public static void main(String[] args) {
        IOutputPlainTextProvider outputPlainTextProvider = System.out::print;

        Engine engine = new Engine(outputPlainTextProvider);

        if (args.length < 1) {
            System.out.println("Пропущен обязательный параметр - файл игры.");
            return;
        }

        try {
            engine.loadStory(args[0], true);
            engine.initGame();
        } catch (IFML2Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            String gamerCommand;
            do {
                System.out.print("\n> ");
                gamerCommand = scanner.nextLine();
            } while (engine.executeGamerCommand(gamerCommand));
        } finally {
            // nothing to do
        }
    }
}
