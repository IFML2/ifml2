package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class Trigger {
    @XmlAttribute(name = "type")
    Type type;
    @XmlElement(name = "instructions")
    private InstructionList instructions = new InstructionList();

    public Type getType() {
        return type;
    }

    public InstructionList getInstructions() {
        return instructions;
    }

    @XmlEnum
    @XmlType(namespace = "Trigger")
    public enum Type {
        @XmlEnumValue(value = "getAccessibleContent")
        GET_ACCESSIBLE_CONTENT
    }
}
