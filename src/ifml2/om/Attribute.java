package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

public class Attribute
{
    @XmlAttribute(name = "name")
    @XmlID
    private String name;
    public String getName() { return name; }

    @XmlAttribute(name = "description")
    private String description;
    public String getDescription() { return description; }

    Attribute()
    {
    }

    @Override
    public String toString()
    {
        return name;
    }
}
