package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFMLEntity;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;

import static ifml2.om.xml.XmlSchemaConstants.*;

@XmlAccessorType(XmlAccessType.NONE)
public class Role extends IFMLEntity
{
    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = ROLE_NAME_ATTRIBUTE)
    @XmlIDREF
    private RoleDefinition roleDefinition; // reference, don't clone

    @XmlElementWrapper(name = ROLE_PROPERTIES_ELEMENT)
    @XmlElement(name = ROLE_PROPERTY_ELEMENT)
    private EventList<Property> properties = new BasicEventList<Property>();

    public Role(RoleDefinition roleDefinition)
    {
        this.roleDefinition = roleDefinition;

        // fill with properties
        for (PropertyDefinition propertyDefinition : roleDefinition.getPropertyDefinitions())
        {
            properties.add(new Property(propertyDefinition, this));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public Role()
    {

    }

    @Override
    public Role clone() throws CloneNotSupportedException
    {
        Role clone = (Role) super.clone(); // flat clone

        // deep clone
        clone.properties = deepCloneEventList(properties, Property.class);

        return clone;
    }

    public RoleDefinition getRoleDefinition()
    {
        return roleDefinition;
    }

    public EventList<Property> getProperties()
    {
        return properties;
    }

    @Override
    public String toString()
    {
        return roleDefinition != null ? roleDefinition.getName() : "не задана";
    }

    public String getName()
    {
        assert roleDefinition != null;
        return roleDefinition.getName();
    }

    public Property tryFindPropertyByDefinition(PropertyDefinition propertyDefinition)
    {
        if (propertyDefinition == null)
        {
            return null;
        }

        for (Property property : properties)
        {
            if (propertyDefinition.getName().equalsIgnoreCase(property.getName()))
            {
                return property;
            }
        }

        return null;
    }

    public Property findPropertyByName(String name)
    {
        for (Property property : properties)
        {
            if (property.getName().equalsIgnoreCase(name))
            {
                return property;
            }
        }

        return null;
    }

    public void copyTo(@NotNull Role role)
    {
        //fixme
    }
}
