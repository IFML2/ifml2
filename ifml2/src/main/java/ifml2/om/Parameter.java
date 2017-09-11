package ifml2.om;

import ifml2.IFMLEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class Parameter extends IFMLEntity implements Cloneable {
    @XmlAttribute(name = "name")
    private String name;

    public Parameter(String name) {
        this.name = name;
    }

    public Parameter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
