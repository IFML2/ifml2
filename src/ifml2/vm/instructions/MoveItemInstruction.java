package ifml2.vm.instructions;

import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.om.Item;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.List;

@XmlRootElement(name = "moveItem")
public class MoveItemInstruction extends Instruction
{
    @XmlAttribute(name = "item")
    String item;
    @XmlAttribute(name = "to")
    String to;

    public static String getTitle()
    {
        return "Переместить предмет";
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Item itemObject = getItemFromExpression(item, runningContext, getTitle(), "предмет", false);
        assert itemObject.getContainer() != null;

        List<Item> collection = (List<Item>) getCollectionFromExpression(to, runningContext, getTitle(), "куда");

        if (collection.contains(itemObject))
        {
            throw new IFML2Exception(CommonUtils.uppercaseFirstLetter(itemObject.getName()) + " уже там.");
        }

        // move item from parent to new collection
        itemObject.moveTo(collection);
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Переместить предмет \"{0}\" в коллекцию \"{1}\"", item, to);
    }
}
