package ifml2.om;

import ifml2.IFMLEntity;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.NONE)
public class Hook extends IFMLEntity
{
    @XmlElement(name = "instructions")
    public InstructionList instructionList = new InstructionList();

    @XmlAttribute(name = "action")
    @XmlIDREF
    private Action action; // reference, don't clone

    @XmlAttribute(name = "objectElement")
    private String objectElement = "";

    @XmlAttribute(name = "type")
    private Type type = Type.INSTEAD; // default value for new hook in editors

    @Override
    protected Hook clone() throws CloneNotSupportedException
    {
        final Hook clone = (Hook) super.clone(); // clone flat

        // clone deep
        clone.instructionList = instructionList.clone();
        // action is reference, already copied

        return clone;
    }

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public String getObjectElement()
    {
        return objectElement;
    }

    public void setObjectElement(String objectElement)
    {
        this.objectElement = objectElement;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public InstructionList getInstructionList()
    {
        return instructionList;
    }

    @Override
    public String toString()
    {
        return String.format("%s: %s (%s)", action, objectElement != null ? objectElement : "", type.getRuName());
    }

    @XmlEnum
    @XmlType(namespace = "Hook")
    public enum Type
    {
        @XmlEnumValue(value = "before")
        BEFORE(0, "до"),
        @XmlEnumValue(value = "after")
        AFTER(1, "после"),
        @XmlEnumValue(value = "instead")
        INSTEAD(2, "вместо");
        public final int sortValue;
        private String ruName;

        Type(int sortValue, String ruName)
        {
            this.sortValue = sortValue;
            this.ruName = ruName;
        }

        public String getRuName()
        {
            return ruName;
        }
    }
}
