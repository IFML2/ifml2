package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class TemplateElement
{
	@XmlAttribute(name="position")
	public int position;

    @XmlAttribute(name = "parameter")
    public String parameter;
    public String getParameter()
    {
        return parameter;
    }
}
