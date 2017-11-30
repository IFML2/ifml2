package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Restriction
{
    @XmlElement(name = "reaction")
    private final InstructionList reaction = new InstructionList();
    private String condition;

    @Override
    public String toString()
    {
        return condition;
    }

    public String getCondition()
    {
        return condition;
    }

    @XmlAttribute(name = "condition")
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public InstructionList getReaction()
    {
        return reaction;
    }
}
