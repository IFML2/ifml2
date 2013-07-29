package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class Parameter
{
    private String name;
    @XmlAttribute(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString()
    {
        return name;
    }
}
