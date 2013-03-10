package ifml2.vm.instructions;

import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Word;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "getItem")
public class GetItemInstruction extends Instruction
{
    private String itemExpression;

    @XmlAttribute(name="item")
    public void setItemExpression(String itemExpression)
    {
        this.itemExpression = itemExpression;
    }

    public String getItemExpression()
    {
        return itemExpression;
    }

	@Override
	public void run(RunningContext runningContext) throws IFML2Exception
	{
        IFMLObject object = getObjectFromExpression(itemExpression, runningContext, getTitle(), "Предмет", false);

        if(object instanceof Location)
        {
            throw new IFML2Exception("Вы не можете взять локацию.");
        }

        final Engine engine = virtualMachine.getEngine();

		Item item = (Item) object;

        if(engine.getInventory().contains(item))
		{
			throw new IFML2Exception(CommonUtils.uppercaseFirstLetter(object.getName()) + " уже в инвентаре.");
		}

		engine.getInventory().add(item);
		// считаем, что предмет в текущей локации
		engine.getCurrentLocation().getItems().remove(item);

		engine.outTextLn("Вы взяли {0}.", item.getName(Word.GramCaseEnum.VP));
	}

    public static String getTitle()
    {
        return "Взять предмет";
    }

    @Override
    public String toString()
    {
        return "Взять предмет: " + itemExpression;
    }
}
