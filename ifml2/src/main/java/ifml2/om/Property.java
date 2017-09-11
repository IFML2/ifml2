package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.IFML2Exception;
import ifml2.IFMLEntity;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.RunningContext;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.NumberValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import static ifml2.om.xml.XmlSchemaConstants.PROPERTY_COLLECTION_ITEM_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.PROPERTY_NAME_ATTRIBUTE;
import static ifml2.om.xml.XmlSchemaConstants.PROPERTY_VALUE_ATTRIBUTE;

@XmlAccessorType(XmlAccessType.NONE)
public class Property extends IFMLEntity {
    @XmlAttribute(name = PROPERTY_NAME_ATTRIBUTE)
    private String name; //can't load as IDREF because this name isn't unique

    @XmlTransient
    private Role parentRole; // reference

    @XmlAttribute(name = PROPERTY_VALUE_ATTRIBUTE)
    private String valueExpression;

    @XmlElement(name = PROPERTY_COLLECTION_ITEM_ELEMENT)
    @XmlIDREF
    private EventList<IFMLObject> collectionItems = new BasicEventList<IFMLObject>(); // references
    @XmlTransient
    private Value value;

    @SuppressWarnings("UnusedDeclaration")
    public Property() {
        // JAXB
    }

    public Property(PropertyDefinition propertyDefinition, Role parentRole) {
        name = propertyDefinition.getName();
        this.parentRole = parentRole;
        valueExpression = propertyDefinition.getValue();

        //parentRole.getProperties().add(this); //it's bad!
    }

    @Override
    protected Property clone() throws CloneNotSupportedException {
        Property clone = (Property) super.clone(); // flat clone

        // deep clone
        clone.value = value != null ? value.clone() : null;
        clone.setCollectionItems(GlazedLists.eventList(collectionItems)); // just copy refs

        return clone;
    }

    public String getName() {
        return name;
    }

    /**
     * JAXB afterUnmarshal listener
     *
     * @param unmarshaller Unmarshaller
     * @param parent       Parent, should be Role
     */
    @SuppressWarnings("UnusedDeclaration")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {
        assert parent instanceof Role;
        parentRole = (Role) parent;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    /**
     * Gets primary expressions (valueExpression and collectionItems) and evaluates it to value considering type
     *
     * @param runningContext Running context
     */
    public void evaluateFromPrimaryExpression(RunningContext runningContext) throws IFML2Exception {
        // get PropertyDefinition
        assert parentRole != null;
        PropertyDefinition propertyDefinition = parentRole.getRoleDefinition().findPropertyDefinitionByName(name);
        switch (propertyDefinition.getType()) {
            case COLLECTION:
                value = new CollectionValue(collectionItems);
                //set parent to items
                for (IFMLObject ifmlObject : collectionItems) {
                    if (ifmlObject instanceof Item) {
                        ((Item) ifmlObject).setContainer(collectionItems);
                    }
                }
                break;

            case LOGIC:
                Value logicValue = ExpressionCalculator.calculate(runningContext, valueExpression);
                if (!(logicValue instanceof BooleanValue)) {
                    throw new IFML2Exception("Выражение \"{0}\" для свойства \"{1}\" не логического типа", valueExpression,
                            propertyDefinition.getName());
                }
                value = logicValue;
                break;

            case NUMBER:
                Value numberValue = ExpressionCalculator.calculate(runningContext, valueExpression);
                if (!(numberValue instanceof NumberValue)) {
                    throw new IFML2Exception("Выражение \"{0}\" для свойства \"{1}\" не числового типа", valueExpression,
                            propertyDefinition.getName());
                }
                value = numberValue;
                break;

            case TEXT:
                Value textValue = ExpressionCalculator.calculate(runningContext, valueExpression);
                if (!(textValue instanceof TextValue)) {
                    throw new IFML2Exception("Выражение \"{0}\" для свойства \"{1}\" не текстового типа", valueExpression,
                            propertyDefinition.getName());
                }
                value = textValue;
                break;

            default:
                throw new IFML2Exception("Неизвестный тип свойства - \"{0}\"", propertyDefinition.getType());
        }
    }

    @Override
    public String toString() {
        return name; /*MessageFormat.format("Свойство \"{0}\" = {1}", name, value);*/
    }

    public PropertyDefinition findDefinition() {
        return parentRole.getRoleDefinition().findPropertyDefinitionByName(name);
    }

    public String getValueExpression() {
        return valueExpression;
    }

    public void setValueExpression(String valueExpression) {
        this.valueExpression = valueExpression;
    }

    public EventList<IFMLObject> getCollectionItems() {
        return collectionItems;
    }

    public void setCollectionItems(EventList<IFMLObject> collectionItems) {
        this.collectionItems = collectionItems;
    }
}
