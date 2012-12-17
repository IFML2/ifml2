package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.om.Property;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;

@XmlRootElement(name = "setProperty")
public class SetPropertyInstruction extends Instruction
{
    @XmlAttribute(name = "object")
    private String object;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "value")
    private String value;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Value objectGenericValue = ExpressionCalculator.calculate(runningContext, object);
        if(!(objectGenericValue instanceof ObjectValue))
        {
            throw new IFML2VMException("Выражение (\"{0}\") для объекта не объектного типа, а {1}",
                    object, objectGenericValue != null ? objectGenericValue.getTypeName() : "пустое");
        }
        ObjectValue objectValue = (ObjectValue) objectGenericValue;
        IFMLObject ifmlObject = objectValue.getValue();

        //IFMLObject ifmlObject = runningContext.getObjectByName(object);
        if(ifmlObject == null)
        {
            throw new IFML2VMException("Объект с именем \"{0}\" не найден", object);
        }

        Property property = ifmlObject.getPropertyByName(name);
        if(property == null)
        {
            throw new IFML2VMException("Свойство с именем \"{0}\" не найдено у объекта {1}", name, object);
        }

        Value calculatedValue = ExpressionCalculator.calculate(runningContext, value);
        property.setValue(calculatedValue);
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Установить свойству {0} значение {1}", name, value);
    }

    public static String getTitle()
    {
        return "Установить свойство объекта";
    }
}
