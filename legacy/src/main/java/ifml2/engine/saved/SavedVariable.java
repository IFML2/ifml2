package ifml2.engine.saved;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class SavedVariable {
    private String name = null;
    private String value = null;

    @SuppressWarnings("UnusedDeclaration")
    public SavedVariable() {
        // for JAXB
    }

    public SavedVariable(String name, String value) {
        this.setName(name);
        this.setValue(value);
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @XmlValue
    public void setValue(String value) {
        this.value = value;
    }
}
