package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;

public class Action
{
    @XmlElementWrapper(name = "templates")
    @XmlElement(name = "template")
    public final EventList<Template> templates = new BasicEventList<Template>();
    public EventList<Template> getTemplates()
    {
        return templates;
    }

    @XmlElementWrapper(name = "restrictions")
    @XmlElement(name = "restriction")
    private final EventList<Restriction> restrictions = new BasicEventList<Restriction>();
    public EventList<Restriction> getRestrictions()
    {
        return restrictions;
    }

	@XmlElement(name="procedureCall")
	public ProcedureCall procedureCall;
    public ProcedureCall getProcedureCall()
    {
        return procedureCall;
    }

    private String name;
    @XmlAttribute(name = "name")
    @XmlID
    public void setName(String name) { this.name = name; }

    public String getName() { return name; }

    @Override
	public String toString()
	{
		return name;
	}
}
