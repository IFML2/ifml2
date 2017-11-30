package ifml2.engine.saved;

import ifml2.om.Item;
import ifml2.om.Property;
import ifml2.om.PropertyDefinition;
import ifml2.om.PropertyDefinition.Type;
import ifml2.om.Role;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;

public class SavedRole {
    private static final Logger LOG = LoggerFactory.getLogger(SavedRole.class);
    @XmlElementWrapper(name = "props")
    @XmlElement(name = "prop")
    private ArrayList<SavedProperty> properties = new ArrayList<SavedProperty>();
    @XmlAttribute(name = "name")
    private String name;

    @SuppressWarnings("UnusedDeclaration")
    public SavedRole() {
        // public no-args constructor for JAXB
    }

    public SavedRole(@NotNull Role role) {
        name = role.getName();
        for (Property property : role.getProperties()) {
            String propertyName = property.getName();
            PropertyDefinition propertyDefinition = role.getRoleDefinition().findPropertyDefinitionByName(propertyName);
            if (propertyDefinition != null) {
                // save only collections
                if (Type.COLLECTION.equals(propertyDefinition.getType())) {
                    properties.add(new SavedProperty(property));
                }
            } else {
                LOG.error("Системная ошибка: в роли {0} не найдено свойство {1}.", role, propertyName);
            }
        }
    }

    public void restore(@NotNull Item item, @NotNull Story.DataHelper dataHelper) {
        Role role = item.findRoleByName(name);
        if (role != null) {
            for (SavedProperty savedProperty : properties) {
                savedProperty.restore(role, dataHelper);
            }
        } else {
            LOG.warn("Не найдена роль \"{0}\" в предмете \"{1}\".", name, item.getId());
        }
    }
}
