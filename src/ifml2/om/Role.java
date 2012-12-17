package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Role
{
    @XmlAttribute(name = ROLE_NAME_ATTRIBUTE)
    @XmlIDREF
    private RoleDefinition roleDefinition;
    public RoleDefinition getRoleDefinition()
    {
        return roleDefinition;
    }

    @XmlElementWrapper(name = ROLE_PROPERTIES_ELEMENT)
    @XmlElement(name = ROLE_PROPERTY_ELEMENT)
    private EventList<Property> properties = new BasicEventList<Property>();
    public EventList<Property> getProperties()
    {
        return properties;
    }

    @Override
    public String toString()
    {
        return "роль " + (roleDefinition != null ? roleDefinition.getName() : "не задана");
    }

    public String getName()
    {
        assert roleDefinition != null;
        return roleDefinition.getName();
    }

    public Property tryFindPropertyByDefinition(PropertyDefinition propertyDefinition)
    {
        if(propertyDefinition == null)
        {
            return null;
        }

        for(Property property : properties)
        {
            if(propertyDefinition.getName().equalsIgnoreCase(property.getName()))
            {
                return property;
            }
        }

        return null;
    }

    public Property getPropertyByName(String name)
    {
        for(Property property : properties)
        {
            if(property.getName().equalsIgnoreCase(name))
            {
                return property;
            }
        }

        return null;
    }
}
