package ifml2.om;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

@XmlAccessorType(XmlAccessType.NONE)
public class Attribute {
    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = "name")
    @XmlID
    private String name;

    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = "description")
    private String description;

    Attribute() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
