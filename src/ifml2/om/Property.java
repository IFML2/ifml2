package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFML2Exception;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.RunningContext;
import ifml2.vm.values.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.text.MessageFormat;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Property
{
    @XmlAttribute(name = PROPERTY_NAME_ATTRIBUTE)
    private String name; //can't load as IDREF because this name isn't unique

    public Property() { }

    public Property(PropertyDefinition propertyDefinition, Role parentRole)
    {
        super();
        name = propertyDefinition.getName();
        this.parentRole = parentRole;
        valueExpression = propertyDefinition.getValue();

        parentRole.getProperties().add(this);
    }

    public String getName()
    {
        return name;
    }

    @XmlTransient
    private Role parentRole;

    /**
     * JAXB afterUnmarshal listener
     * @param unmarshaller Unmarshaller
     * @param parent Parent, should be Role
     */
    @SuppressWarnings("UnusedDeclaration")
    private void afterUnmarshal(final Unmarshaller unmarshaller,
                                final Object parent)
    {
        assert parent instanceof Role;
        parentRole = (Role) parent;
    }

    @XmlAttribute(name = PROPERTY_VALUE_ATTRIBUTE)
    private String valueExpression;

    @XmlElement(name = PROPERTY_COLLECTION_ITEM_ELEMENT)
    @XmlIDREF
    private EventList<IFMLObject> collectionItems = new BasicEventList<IFMLObject>();

    @XmlTransient
    private Value value;

    public Value getValue()
    {
        return value;
    }

    public void setValue(Value value)
    {
        this.value = value;
    }

    /**
     * Gets primary expressions (valueExpression and collectionItems) and evaluates it to value considering type
     * @param runningContext Running context
     */
    public void evaluateFromPrimaryExpression(RunningContext runningContext) throws IFML2Exception
    {
        // get PropertyDefinition
        assert parentRole != null;
        PropertyDefinition propertyDefinition = parentRole.getRoleDefinition().getPropertyDefinitionByName(name);
        switch(propertyDefinition.getType())
        {
            case COLLECTION:
                value = new CollectionValue(collectionItems);
                //set parent to items
                for(IFMLObject ifmlObject : collectionItems)
                {
                    if(ifmlObject instanceof Item)
                    {
                        ((Item) ifmlObject).setParent(collectionItems);
                    }
                }
                break;

            case LOGIC:
                Value logicValue = ExpressionCalculator.calculate(runningContext, valueExpression);
                if(!(logicValue instanceof BooleanValue))
                {
                    throw new IFML2Exception("Выражение \"{0}\" для свойства \"{1}\" не логического типа",
                            valueExpression, propertyDefinition.getName());
                }
                value = logicValue;
                break;

            case NUMBER:
                Value numberValue = ExpressionCalculator.calculate(runningContext, valueExpression);
                if(!(numberValue instanceof NumberValue))
                {
                    throw new IFML2Exception("Выражение \"{0}\" для свойства \"{1}\" не числового типа",
                            valueExpression, propertyDefinition.getName());
                }
                value = numberValue;
                break;

            case TEXT:
                Value textValue = ExpressionCalculator.calculate(runningContext, valueExpression);
                if(!(textValue  instanceof TextValue))
                {
                    throw new IFML2Exception("Выражение \"{0}\" для свойства \"{1}\" не текстового типа",
                            valueExpression, propertyDefinition.getName());
                }
                value = textValue;
                break;

            default:
                throw new IFML2Exception("Неизвестный тип свойства - \"{0}\"", propertyDefinition.getType());
        }
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Свойство \"{0}\" = {1}", name, value);
    }
}
