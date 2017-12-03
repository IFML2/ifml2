package ifml2.om;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import ifml2.IFMLEntity;
import ifml2.vm.values.Value;

@XmlAccessorType(XmlAccessType.NONE)
public class ProcedureVariable extends IFMLEntity implements Cloneable {
    @XmlAttribute(name = "name")
    private String name;
    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = "initialValue")
    private String initialValue;
    @XmlTransient
    private Value value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    protected ProcedureVariable clone() throws CloneNotSupportedException {
        ProcedureVariable clone = (ProcedureVariable) super.clone(); // clone flat fields

        // clone objects
        clone.value = value.clone();

        return clone;
    }
}
