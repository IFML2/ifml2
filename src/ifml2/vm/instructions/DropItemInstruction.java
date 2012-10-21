package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Word;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dropItem")
public class DropItemInstruction extends Instruction
{
    @XmlAttribute(name="item")
    private String itemExpression;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        IFMLObject object = getObjectFromExpression(itemExpression, runningContext, getTitle(), "Предмет", false);

        if(object instanceof Location)
        {
            throw new IFML2Exception("Вы не можете бросить локацию.");
        }

        Item item = (Item) object;

        final Engine engine = virtualMachine.getEngine();

        if(!engine.getInventory().contains(item))
        {
            throw new IFML2Exception("У Вас нет " + item.getName(Word.GramCaseEnum.RP) + ".");
        }

        engine.getCurrentLocation().getItems().add(item);

        engine.getInventory().remove(item);

        engine.outTextLn("Вы выбросили {0}.", item.getName(Word.GramCaseEnum.VP));
    }

    public static String getTitle()
    {
        return "Выбросить предмет";
    }

    @Override
    public String toString()
    {
        return "Выбросить предмет " + itemExpression;
    }
}
