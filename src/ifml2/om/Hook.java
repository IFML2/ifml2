package ifml2.om;

import ifml2.om.xml.xmladapters.ActionRefsAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Hook
{
    private Action action;
    @XmlAttribute(name = "action")
    @XmlJavaTypeAdapter(value = ActionRefsAdapter.class)
    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
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
