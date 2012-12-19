package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.Item;
import ifml2.vm.RunningContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Item itemObject = getItemFromExpression(item, runningContext, getTitle(), "предмет", false);
        List collection = getCollectionFromExpression(to, runningContext, getTitle(), "куда");

        //todo!
        /*
        * Вопрос в том, как определять, откуда предмет нужно извлечь, чтобы переместить?
        * Или хранить ему ссылку на parent (тогда знать о коллекции, в которой о находится)
        * Или в инструкции указывать, откуда перемещать (а это не верно! зачем автору знать об этом?)
        *
        * */
        throw new NotImplementedException();
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Перемещение предмета \"{0}\" в локацию или коллекцию \"{1}\"", item, to);
    }

    public static String getTitle()
    {
        return "Перемещение предмета";
    }
}
