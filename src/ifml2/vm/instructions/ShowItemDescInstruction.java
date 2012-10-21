package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.om.Location;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "showItemDesc")
public class ShowItemDescInstruction extends Instruction
{
	@XmlAttribute(name="item")
    private
    String itemExpression;

	@Override
	public void run(RunningContext runningContext) throws IFML2Exception
	{
		IFMLObject object = getObjectFromExpression(itemExpression, runningContext, getTitle(), "Предмет", false);

        if(object instanceof Location)
        {
            virtualMachine.showLocName((Location) object);
        }
        else
        {
		    String description = object.getDescription();
            if (description == null || "".equals(description))
            {
                description = "Ничего особенного.";
            }
            virtualMachine.getEngine().outTextLn(description);
        }
	}

    public static String getTitle()
    {
        return "Вывести описание предмета";
    }
}
