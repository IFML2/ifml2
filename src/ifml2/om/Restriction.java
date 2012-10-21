package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Restriction
{
    @XmlAttribute(name = "condition")
    private String condition;
    public String getCondition() { return condition; }

    @XmlElement(name = "reaction")
    private final InstructionList reaction = new InstructionList();
    public InstructionList getReaction() { return reaction; }
}
