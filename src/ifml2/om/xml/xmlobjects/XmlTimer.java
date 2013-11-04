package ifml2.om.xml.xmlobjects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "timer")
public class XmlTimer
{
    @XmlAttribute(name = "quantity")
    public Integer quantity;
    @XmlAttribute(name = "type")
    public TimerTypeEnum type;
    @XmlAttribute(name = "name")
    public String name;
    @XmlAttribute(name = "time")
    public Date time;

    @SuppressWarnings("UnusedDeclaration")
    public XmlTimer()
    {
        //default const for JAXB
    }

    public XmlTimer(String name, Date time)
    {
        type = TimerTypeEnum.REAL_TIME;
        this.name = name;
        this.time = time;
    }

    public XmlTimer(String name, Integer quantity)
    {
        type = TimerTypeEnum.ACTIONS;
        this.name = name;
        this.quantity = quantity;
    }

    @XmlEnum
    public enum TimerTypeEnum
    {
        @XmlEnumValue(value = "real-time")
        REAL_TIME,
        @XmlEnumValue(value = "actions")
        ACTIONS
    }
}
