package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.Location;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "showLocName")
public class ShowLocNameInstruction extends Instruction
{
	@XmlAttribute(name="location")
    private
    String locationExpression;

	@Override
	public void run(RunningContext runningContext) throws IFML2Exception
	{
        Location location = getLocationFromExpression(locationExpression, runningContext, getTitle(), "Локация", false);

		virtualMachine.showLocName(location);
	}

    public static String getTitle()
    {
        return "Вывести описание локации";
    }
}
