package ifml2.engine.saved;

import ifml2.om.Item;
import ifml2.om.Role;
import ifml2.om.Story;
import ifml2.storage.domain.SavedItem;
import ifml2.storage.domain.SavedRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

public class XmlSavedItem implements SavedItem {

    private static final Logger LOG = LoggerFactory.getLogger(XmlSavedItem.class);

    @XmlAttribute(name = "id")
    private String id;

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role", type = XmlSavedRole.class)
    private List<SavedRole> roles = new ArrayList<>();

    @SuppressWarnings("UnusedDeclaration")
    public XmlSavedItem() {
        // public no-args constructor for JAXB
    }

    public XmlSavedItem(Item item) {
        id = item.getId();
        item.getRoles().forEach(role -> roles.add(new XmlSavedRole()));
        for (Role role : item.getRoles()) {
            roles.add(new XmlSavedRole(role));
        }
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<SavedRole> getRoles() {
        return (List<SavedRole>) roles;
    }

    public void setRoles(final List<SavedRole> roles) {
        this.roles = roles;
    }

    public void restore(Story.DataHelper dataHelper) {
        Item item = dataHelper.findItemById(id);
        if (item != null) {
            roles.forEach(role -> ((XmlSavedRole) role).restore(item, dataHelper));
        } else {
            LOG.warn("Item with ID \"{0}\" not found.", id);
        }
    }
}
