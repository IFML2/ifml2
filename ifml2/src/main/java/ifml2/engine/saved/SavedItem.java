package ifml2.engine.saved;

import ifml2.om.Item;
import ifml2.om.Role;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

public class SavedItem {
    private static final Logger LOG = LoggerFactory.getLogger(SavedItem.class);
    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private List<SavedRole> roles = new ArrayList<SavedRole>();
    @XmlAttribute(name = "id")
    private String id;

    @SuppressWarnings("UnusedDeclaration")
    public SavedItem() {
        // public no-args constructor for JAXB
    }

    public SavedItem(@NotNull Item item) {
        id = item.getId();
        for (Role role : item.getRoles()) {
            roles.add(new SavedRole(role));
        }
    }

    public void restore(@NotNull Story.DataHelper dataHelper) {
        Item item = dataHelper.findItemById(id);
        if (item != null) {
            for (SavedRole savedRole : roles) {
                savedRole.restore(item, dataHelper);
            }
        } else {
            LOG.warn("Не найден предмет с ид \"{0}\".", id);
        }
    }
}
