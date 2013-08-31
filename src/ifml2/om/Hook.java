package ifml2.om;

import javax.xml.bind.annotation.*;

public class Hook
{
    @XmlElement(name = "instructions")
    public final InstructionList instructionList = new InstructionList();
    private Action action;
    private String objectElement = "";
    private HookTypeEnum type = HookTypeEnum.INSTEAD; // default value for new hook in editors

    public Action getAction()
    {
        return action;
    }

    @XmlAttribute(name = "action")
    @XmlIDREF
    public void setAction(Action action)
    {
        this.action = action;
    }

    public String getObjectElement()
    {
        return objectElement;
    }

    @XmlAttribute(name = "objectElement")
    public void setObjectElement(String objectElement)
    {
        this.objectElement = objectElement;
    }

    public HookTypeEnum getType()
    {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setType(HookTypeEnum type)
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
        return String.format("%s: %s (%s)", action, objectElement, type.ruName);
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
}
