package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.om.xml.xmladapters.ProceduresAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

@XmlRootElement(name = "library")
public class Library 
{
	@XmlTransient
    public String path;

    @XmlAttribute(name = "name")
    private String name;

    public String getName()
    {
        return name;
    }

    @XmlElementWrapper(name = "attribute-definitions")
    @XmlElement(name = "attribute-definition")
    public EventList<Attribute> attributes = new BasicEventList<Attribute>();

    public EventList<Attribute> getAttributes()
    {
        return attributes;
    }

    @XmlElementWrapper(name = "role-definitions")
    @XmlElement(name = "role-definition")
    public EventList<RoleDefinition> roleDefinition = new BasicEventList<RoleDefinition>();

    @XmlElementWrapper(name = "actions")
	@XmlElement(name = "action")
	public EventList<Action> actions = new BasicEventList<Action>();

	@XmlJavaTypeAdapter(value = ProceduresAdapter.class)
	public HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();

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
