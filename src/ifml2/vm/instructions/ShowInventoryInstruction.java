package ifml2.vm.instructions;

import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "showInventory")
public class ShowInventoryInstruction extends Instruction
{
	@Override
	public void run(RunningContext runningContext) throws IFML2VMException
	{
		virtualMachine.showInventory();
	}

    public static String getTitle()
    {
        return "Вывести инвентарь";
    }
}
