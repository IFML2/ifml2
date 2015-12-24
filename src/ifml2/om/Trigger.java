package ifml2.om;

import javax.xml.bind.annotation.*;

public class Trigger
{
    public Type getType()
    {
        return type;
    }

    @XmlEnum
    @XmlType(namespace = "Trigger")
    public enum Type
    {
        @XmlEnumValue(value = "getAccessibleContent")
        GET_ACCESSIBLE_CONTENT
    }

    @XmlAttribute(name = "type")
    Type type;

    @XmlElement(name = "instructions")
    private InstructionList instructions = new InstructionList();
    public InstructionList getInstructions() { return instructions; }
}
