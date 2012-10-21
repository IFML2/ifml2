package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

public class PropertyDefinition
{
    @XmlAttribute(name = "name")
    public String name;

    @XmlAttribute(name = "description")
    public String description;

    @XmlAttribute(name = "type")
    public PropertyTypeEnum type;

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
}
