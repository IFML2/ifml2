package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class TemplateElement implements Cloneable
{
	@XmlAttribute(name="position")
	public int position;

    protected String parameter;
    @XmlAttribute(name = "parameter")
    public String getParameter()
    {
        return parameter;
    }
    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    @Override
    public TemplateElement clone() throws CloneNotSupportedException
    {
        return (TemplateElement) super.clone();
    }
}
