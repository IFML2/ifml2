package ifml2.engine.saved;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

public class SavedItem
{
    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private List<SavedRole> roles = new ArrayList<SavedRole>();

    @SuppressWarnings("UnusedDeclaration")
    public SavedItem()
    {
        // public no-args constructor for JAXB
    }

    @XmlAttribute(name = "id")
    private String id = null;

    public SavedItem(String id)
    {
        this.id = id;
    }

    public void addRole(SavedRole savedRole)
    {
        getRoles().add(savedRole);
    }

    public String getId()
    {
        return id;
    }

    public List<SavedRole> getRoles()
    {
        return roles;
    }
}
