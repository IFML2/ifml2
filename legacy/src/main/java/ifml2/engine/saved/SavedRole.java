package ifml2.engine.saved;

import java.util.ArrayList;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ifml2.om.Item;
import ifml2.om.Property;
import ifml2.om.PropertyDefinition;
import ifml2.om.PropertyDefinition.Type;
import ifml2.om.Role;
import ifml2.om.Story;

public class SavedRole {
    private static final Logger LOG = LoggerFactory.getLogger(SavedRole.class);
    @XmlElementWrapper(name = "props")
    @XmlElement(name = "prop")
    private ArrayList<SavedProperty> properties = new ArrayList<>();
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
        Optional<Role> optRole = Optional.ofNullable(item.findRoleByName(name));
        if (!optRole.isPresent()) {
            LOG.warn("Не найдена роль \"{0}\" в предмете \"{1}\".", name, item.getId());
        }
        optRole.ifPresent(role -> properties.forEach(prop -> prop.restore(role, dataHelper)));
    }
}
