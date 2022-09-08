package ifml2.vm.instructions;

import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.IFMLEntity;
import ifml2.om.Item;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.List;

@XmlRootElement(name = "moveItem")
@IFML2Instruction(title = "Переместить предмет")
public class MoveItemInstruction extends Instruction
{
    private String itemExpr = "";
    private String toCollectionExpr = "";

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Item item = getItemFromExpression(itemExpr, runningContext, getTitle(), "предмет", false);
        assert item.getContainer() != null;

        List<? extends IFMLEntity> collection = getCollectionFromExpression(toCollectionExpr, runningContext, getTitle(), "куда");

        if (collection.contains(item))
        {
            throw new IFML2Exception(CommonUtils.uppercaseFirstLetter(item.getName()) + " уже там.");
        }

        // move item from parent to new collection
        item.moveTo((List<Item>) collection);
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Переместить предмет \"{0}\" в коллекцию \"{1}\"", itemExpr, toCollectionExpr);
    }

    public String getItemExpr()
    {
        return itemExpr;
    }

    @XmlAttribute(name = "item")
    public void setItemExpr(String itemExpr)
    {
        this.itemExpr = itemExpr;
    }

    public String getToCollectionExpr()
    {
        return toCollectionExpr;
    }

    @XmlAttribute(name = "to")
    public void setToCollectionExpr(String toCollectionExpr)
    {
        this.toCollectionExpr = toCollectionExpr;
    }
}
