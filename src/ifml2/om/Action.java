package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import java.util.ArrayList;

public class Action
{
    @XmlElementWrapper(name = "templates")
    @XmlElement(name = "template")
    private final EventList<Template> templates = new BasicEventList<Template>();
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
	public ProcedureCall procedureCall = new ProcedureCall();
    public ProcedureCall getProcedureCall()
    {
        return procedureCall;
    }

    private String name;
    @XmlAttribute(name = "name")
    @XmlID
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    private String description;
    @XmlAttribute(name = "description")
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getDescription()
    {
        return description;
    }


    @Override
	public String toString()
	{
		return name;
	}

    public Object[] getAllParameters()
    {
        ArrayList<Object> parameters = new ArrayList<Object>();

        for (Template template : templates)
        {
            for (TemplateElement element : template.getElements())
            {
                if (element instanceof ObjectTemplateElement && element.getParameter() != null)
                {
                    parameters.add(element.getParameter());
                }
            }
        }

        return parameters.toArray();
    }
}
