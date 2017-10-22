package ifml2.engine.saved;

import ifml2.om.Item;
import ifml2.om.Property;
import ifml2.om.PropertyDefinition;
import ifml2.om.PropertyDefinition.Type;
import ifml2.om.Role;
import ifml2.om.Story;
import ifml2.storage.domain.SavedProperty;
import ifml2.storage.domain.SavedRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

public class XmlSavedRole implements SavedRole {

    private static final Logger LOG = LoggerFactory.getLogger(XmlSavedRole.class);

    @XmlAttribute(name = "name")
    private String name;

    @XmlElementWrapper(name = "props")
    @XmlElement(name = "prop", type = XmlSavedProperty.class)
    private List<SavedProperty> properties = new ArrayList<>();

    @SuppressWarnings("UnusedDeclaration")
    public XmlSavedRole() {
        // public no-args constructor for JAXB
    }

    public XmlSavedRole(Role role) {
        name = role.getName();
        for (Property property : role.getProperties()) {
            String propertyName = property.getName();
            PropertyDefinition propertyDefinition = role.getRoleDefinition().findPropertyDefinitionByName(propertyName);
            if (propertyDefinition != null) {
                // save only collections
                if (Type.COLLECTION.equals(propertyDefinition.getType())) {
                    properties.add(new XmlSavedProperty(property));
                }
            } else {
                LOG.error("Системная ошибка: в роли {0} не найдено свойство {1}.", role, propertyName);
            }
        }
    }

    public String getName(){
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<SavedProperty> getProperties() {
        return properties;
    }

    public void setProperties(final List<SavedProperty> properties) {
        this.properties = properties;
    }

    public void restore(Item item, Story.DataHelper dataHelper) {
        Role role = item.findRoleByName(name);
        if (role != null) {
            properties.forEach(property -> ((XmlSavedProperty) property).restore(role, dataHelper));
        } else {
            LOG.warn("Role \"{0}\" not found for \"{1}\".", name, item.getId());
        }
    }
}
