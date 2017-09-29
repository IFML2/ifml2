package ifml2.players;

import java.util.Scanner;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.engine.featureproviders.text.OutputPlainTextProvider;
import ifml2.service.ServiceRegistry;

public class ConsolePlayer {
    /**
     * @param args First arg is story file path
     */
    public static void main(String[] args) {
        Engine engine = ServiceRegistry.getEngine(System.out::print, null);

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

        Scanner scanner = new Scanner(System.in);
        String gamerCommand;
        do {
            System.out.print("\n> ");
            gamerCommand = scanner.nextLine();
        } while (engine.executeGamerCommand(gamerCommand));
        scanner.close();
    }
}
