package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.text.MessageFormat;

public class PropertyDefinition
{
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "value")
    private String value;

    public String getName()
    {
        return name;
    }

    @XmlAttribute(name = "description")
    public String description;

    @XmlAttribute(name = "type")
    private PropertyTypeEnum type;
    public PropertyTypeEnum getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }

    @XmlEnum
    public enum PropertyTypeEnum
    {
        @XmlEnumValue(value = "text")
        TEXT,
        @XmlEnumValue(value = "number")
        NUMBER,
        @XmlEnumValue(value = "logic")
        LOGIC,
        @XmlEnumValue(value = "collection")
        COLLECTION
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Определение свойства \"{0}\" типа {1}", name,  type);
    }
}
