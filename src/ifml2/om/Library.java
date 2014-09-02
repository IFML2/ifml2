package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.FormatLogger;

import javax.xml.bind.annotation.*;

import static ifml2.om.xml.XmlSchemaConstants.LIBRARY_PROCEDURES_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.PROCEDURES_PROCEDURE_ELEMENT;

@XmlRootElement(name = "library")
public class Library
{
    public static final FormatLogger LOG = FormatLogger.getLogger(Library.class);

    @XmlElementWrapper(name = "attribute-definitions")
    @XmlElement(name = "attribute-definition")
    public final EventList<Attribute> attributes = new BasicEventList<Attribute>();

    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    public final EventList<Action> actions = new BasicEventList<Action>();

    //@XmlJavaTypeAdapter(value = ProceduresAdapter.class)
    //public final HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();
    @XmlElementWrapper(name = LIBRARY_PROCEDURES_ELEMENT)
    @XmlElement(name = PROCEDURES_PROCEDURE_ELEMENT)
    public final EventList<Procedure> procedures = new BasicEventList<Procedure>();

    @XmlElementWrapper(name = "role-definitions")
    @XmlElement(name = "role-definition")
    public EventList<RoleDefinition> roleDefinitions = new BasicEventList<RoleDefinition>();
    private String path;
    private String name;

    public Library()
    {
        LOG.trace("Library() :: path = \"{0}\", name = \"{0}\"", path, name);
    }

    @XmlTransient
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        LOG.trace("setPath(path = \"{0}\")", path);
        this.path = path;
    }

    @XmlAttribute(name = "name")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        LOG.trace("setName(name = \"{0}\")", name);
        this.name = name;
    }

    public EventList<Attribute> getAttributes()
    {
        return attributes;
    }

    public EventList<RoleDefinition> getRoleDefinitions()
    {
        return roleDefinitions;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public Attribute getAttributeByName(String name)
    {
        if (name != null)
        {
            for (Attribute attribute : attributes)
            {
                if (name.equalsIgnoreCase(attribute.getName()))
                {
                    return attribute;
                }
            }
        }
        return null;
    }

    public Action getActionByName(String name)
    {
        if (name != null)
        {
            for (Action action : actions)
            {
                if (name.equalsIgnoreCase(action.getName()))
                {
                    return action;
                }
            }
        }
        return null;
    }

    public RoleDefinition getRoleDefinitionByName(String name)
    {
        if (name != null)
        {
            for (RoleDefinition roleDefinition : roleDefinitions)
            {
                if (name.equalsIgnoreCase(roleDefinition.getName()))
                {
                    return roleDefinition;
                }
            }
        }
        return null;
    }
}
