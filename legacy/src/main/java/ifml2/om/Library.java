package ifml2.om;

import static ifml2.om.xml.XmlSchemaConstants.LIBRARY_PROCEDURES_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.PROCEDURES_PROCEDURE_ELEMENT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

@XmlRootElement(name = "library")
public class Library {
    public static final Logger LOG = LoggerFactory.getLogger(Library.class);

    @XmlElementWrapper(name = "attribute-definitions")
    @XmlElement(name = "attribute-definition")
    public final EventList<Attribute> attributes = new BasicEventList<Attribute>();

    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    public final EventList<Action> actions = new BasicEventList<Action>();

    @XmlElementWrapper(name = LIBRARY_PROCEDURES_ELEMENT)
    @XmlElement(name = PROCEDURES_PROCEDURE_ELEMENT)
    public final EventList<Procedure> procedures = new BasicEventList<Procedure>();

    @XmlElementWrapper(name = "role-definitions")
    @XmlElement(name = "role-definition")
    public EventList<RoleDefinition> roleDefinitions = new BasicEventList<RoleDefinition>();
    private String path;
    private String name;

    public Library() {
        LOG.trace("Library() :: path = \"{}\", name = \"{}\"", path, name);
    }

    @XmlTransient
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        LOG.trace("setPath(path = \"{}\")", path);
        this.path = path;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        LOG.trace("setName(name = \"{}\")", name);
        this.name = name;
    }

    public EventList<Attribute> getAttributes() {
        return attributes;
    }

    public EventList<RoleDefinition> getRoleDefinitions() {
        return roleDefinitions;
    }

    @Override
    public String toString() {
        return name;
    }

    public Attribute getAttributeByName(String name) {
        return name == null ? null
                : attributes.stream().filter(att -> name.equalsIgnoreCase(att.getName())).findFirst().orElse(null);
    }

    public Action getActionByName(String name) {
        return name == null ? null
                : actions.stream().filter(act -> name.equalsIgnoreCase(act.getName())).findFirst().orElse(null);
    }

    public RoleDefinition getRoleDefinitionByName(String name) {
        return name == null ? null
                : roleDefinitions.stream().filter(roleDefinition -> name.equalsIgnoreCase(roleDefinition.getName()))
                        .findFirst().orElse(null);
    }
}
