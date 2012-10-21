package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.Location;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "goToLoc")
public class GoToLocInstruction extends Instruction
{
	@XmlAttribute(name="location")
    private
    String locationExpression;

	@Override
	public void run(RunningContext runningContext) throws IFML2Exception
	{
        Location location = getLocationFromExpression(locationExpression, runningContext, getTitle(), "Локация", true);

        if(location == null)
        {
            throw new IFML2Exception("Туда нельзя пройти.");
        }

		virtualMachine.getEngine().setCurrentLocation(location);

		// run sho loc name instruction
		virtualMachine.showLocName(location);
	}

    public static String getTitle()
    {
        return "Перейти в локацию";
    }
}
