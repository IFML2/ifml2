package ifml2.om;

import ifml2.vm.values.Value;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class ProcedureVariable
{
    @XmlAttribute(name = "name")
    private String name;
    public String getName()
    {
        return name;
    }

    @XmlAttribute(name = "initialValue")
    private String initialValue;
    public String getInitialValue()
    {
        return initialValue;
    }

    @XmlTransient
    private Value value;
    public Value getValue()
    {
        return value;
    }
    public void setValue(Value value)
    {
        this.value = value;
    }
}
