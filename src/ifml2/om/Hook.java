package ifml2.om;

import javax.xml.bind.annotation.*;

public class Hook
{
    private Action action;
    @XmlAttribute(name = "action")
    @XmlIDREF
    public void setAction(Action action)
    {
        this.action = action;
    }
    public Action getAction()
    {
        return action;
    }

    @XmlAttribute(name = "objectElement")
    public void setObjectElement(String objectElement)
    {
        this.objectElement = objectElement;
    }
    private String objectElement;
    public String getObjectElement()
    {
        return objectElement;
    }

    @XmlAttribute(name = "type")
    public void setType(HookTypeEnum type)
    {
        this.type = type;
    }
    private HookTypeEnum type = HookTypeEnum.INSTEAD; // default value for new hook in editors
    public HookTypeEnum getType()
    {
        return type;
    }

    @XmlElement(name = "instructions")
    public final InstructionList instructionList = new InstructionList();

    public InstructionList getInstructionList()
    {
        return instructionList;
    }

    @XmlEnum
    public enum HookTypeEnum
    {
        @XmlEnumValue(value = "before")
        BEFORE(0, "до"),
        @XmlEnumValue(value = "after")
        AFTER(1, "после"),
        @XmlEnumValue(value = "instead")
        INSTEAD(2, "вместо");

        public final int sortValue;
        public final String ruName;

        HookTypeEnum(int sortValue, String ruName)
        {
            this.sortValue = sortValue;
            this.ruName = ruName;
        }
    }

    @Override
    public String toString()
    {
        return String.format("%s: %s (%s)", action, objectElement, type.ruName);
    }
}
