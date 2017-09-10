package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.ROLE_DEFINITION_ATTRIBUTES_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.ROLE_DEFINITION_ATTRIBUTE_ELEMENT;

@XmlAccessorType(XmlAccessType.NONE)
public class RoleDefinition {
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyDefinition> propertyDefinitions = new BasicEventList<PropertyDefinition>();

    @XmlAttribute(name = "description")
    private String description;

    @XmlElementWrapper(name = "triggers")
    @XmlElement(name = "trigger")
    private List<Trigger> triggers = new BasicEventList<Trigger>();

    @XmlAttribute(name = "name")
    @XmlID
    private String name;

    @XmlElementWrapper(name = ROLE_DEFINITION_ATTRIBUTES_ELEMENT)
    @XmlElement(name = ROLE_DEFINITION_ATTRIBUTE_ELEMENT)
    @XmlIDREF
    private EventList<Attribute> attributes = new BasicEventList<Attribute>();

    public String getName() {
        return name;
    }

    public EventList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(EventList<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return /*"определение роли " + */name;
    }

    public PropertyDefinition findPropertyDefinitionByName(String name) {
        assert name != null;
        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            if (name.equalsIgnoreCase(propertyDefinition.getName())) {
                return propertyDefinition;
            }
        }
        return null;
    }

    public Trigger getTrigger(Trigger.Type triggerType) {
        for (Trigger trigger : getTriggers()) {
            if (triggerType.equals(trigger.getType())) {
                return trigger;
            }
        }

        return null;
    }

    public List<PropertyDefinition> getPropertyDefinitions() {
        return propertyDefinitions;
    }

    public String getDescription() {
        return description;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }
}
