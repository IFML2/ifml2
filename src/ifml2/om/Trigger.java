package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

public class Trigger
{
    public TriggerTypeEnum getType()
    {
        return type;
    }

    @XmlEnum
    public enum TriggerTypeEnum
    {
        @XmlEnumValue(value = "getAccessibleContent")
        GET_ACCESSIBLE_CONTENT
    }

    @XmlAttribute(name = "type")
    TriggerTypeEnum type;

    @XmlElement(name = "instructions")
    private InstructionList instructions = new InstructionList();
    public InstructionList getInstructions() { return instructions; }
}
