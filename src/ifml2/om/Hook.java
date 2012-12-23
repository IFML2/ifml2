package ifml2.om;

import javax.xml.bind.annotation.*;

public class Hook
{
    @XmlAttribute(name = "action")
    @XmlIDREF
    private Action action;
    public Action getAction()
    {
        return action;
    }

    @XmlAttribute(name = "objectElement")
    public String objectElement;

    @XmlAttribute(name = "type")
    public HookTypeEnum type;

    @XmlElement(name = "instructions")
    public final InstructionList instructionList = new InstructionList();

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
        return String.format("%s: %s (%s)", action.getName(), objectElement, type.ruName);
    }
}
