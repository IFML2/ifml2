package ifml2.interfaces;

import java.util.Scanner;

public class TextInterface extends Interface
{
	private final Scanner scanner = new Scanner(System.in);
    //public Engine engine = null;
	
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

    /*@Override
	public String getGamerCommand()
	{
		outputText("\n> "); 
		return scanner.nextLine();
	}*/
}
