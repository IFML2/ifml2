package ifml2.engine.savedGame;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Variable
{
    private String name = null;
    private String value = null;

    public Variable()
    {
    }

    public Variable(String name, String value)
    {
        this();
        this.setName(name);
        this.setValue(value);
    }

    public String getName()
    {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    @XmlValue
    public void setValue(String value)
    {
        this.value = value;
    }
}
