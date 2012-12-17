package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.*;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.ROLE_DEFINITION_ATTRIBUTES_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.ROLE_DEFINITION_ATTRIBUTE_ELEMENT;

class RoleDefinition
{
    @XmlAttribute(name = "name")
    @XmlID
    private String name;
    public String getName()
    {
        return name;
    }

    @XmlAttribute(name = "description")
    public String description;

    private EventList<Attribute> attributes = new BasicEventList<Attribute>();
    @XmlElementWrapper(name = ROLE_DEFINITION_ATTRIBUTES_ELEMENT)
    @XmlElement(name = ROLE_DEFINITION_ATTRIBUTE_ELEMENT)
    @XmlIDREF
    public EventList<Attribute> getAttributes() { return attributes; }
    public void setAttributes(EventList<Attribute> attributes) { this.attributes = attributes; }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<PropertyDefinition> propertyDefinitions = new BasicEventList<PropertyDefinition>();

    @XmlElementWrapper(name = "triggers")
    @XmlElement(name = "trigger")
    public List<Trigger> triggers = new BasicEventList<Trigger>();

    @Override
    public String toString()
    {
        return "определение роли " + name;
    }

    public PropertyDefinition getPropertyDefinitionByName(String name)
    {
        assert name != null;
        for(PropertyDefinition propertyDefinition : propertyDefinitions)
        {
            if(name.equalsIgnoreCase(propertyDefinition.getName()))
            {
               return propertyDefinition;
            }
        }
        return null;
    }

    public Trigger getTrigger(Trigger.TriggerTypeEnum triggerType)
    {
        for (Trigger trigger : triggers)
        {
            if(triggerType.equals(trigger.getType()))
            {
                return trigger;
            }
        }

        return null;
    }
}
