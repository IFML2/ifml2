package ifml2.om;

import ifml2.IFMLEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Restriction extends IFMLEntity {
    @XmlElement(name = "reaction")
    private InstructionList reaction = new InstructionList();
    @XmlAttribute(name = "condition")
    private String condition;

    @Override
    protected Restriction clone() throws CloneNotSupportedException {
        Restriction clone = (Restriction) super.clone(); // flat clone

        // deep clone
        clone.reaction = reaction.clone();

        return clone;
    }

    @Override
    public String toString() {
        return condition;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public InstructionList getReaction() {
        return reaction;
    }
}
