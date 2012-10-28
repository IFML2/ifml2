package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.om.xml.xmladapters.ProceduresAdapter;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

@XmlRootElement(name = "library")
public class Library 
{
    public static final Logger LOG = Logger.getLogger(Library.class);

    public Library()
    {
        LOG.trace(String.format("Library() :: path = \"%s\", name = \"%s\"", path, name));
    }

    private String path;
    @XmlTransient
    public String getPath()
    {
        return path;
    }
    public void setPath(String path)
    {
        LOG.trace(String.format("setPath(path = \"%s\")", path));
        this.path = path;
    }

    private String name;
    @XmlAttribute(name = "name")
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        LOG.trace(String.format("setName(name = \"%s\")", name));
        this.name = name;
    }

    @XmlElementWrapper(name = "attribute-definitions")
    @XmlElement(name = "attribute-definition")
    public final EventList<Attribute> attributes = new BasicEventList<Attribute>();

    public EventList<Attribute> getAttributes()
    {
        return attributes;
    }

    @XmlElementWrapper(name = "role-definitions")
    @XmlElement(name = "role-definition")
    public EventList<RoleDefinition> roleDefinition = new BasicEventList<RoleDefinition>();

    @XmlElementWrapper(name = "actions")
	@XmlElement(name = "action")
	public final EventList<Action> actions = new BasicEventList<Action>();

	@XmlJavaTypeAdapter(value = ProceduresAdapter.class)
	public final HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();

    @Override
    public String toString()
    {
        return name;
    }

    public Attribute getAttributeByName(String name)
    {
        if(name != null)
        {
            for(Attribute attribute : attributes)
            {
                if(name.equalsIgnoreCase(attribute.getName()))
                {
                    return attribute;
                }
            }
        }
        return null;
    }
}
