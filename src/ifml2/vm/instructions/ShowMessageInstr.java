package ifml2.vm.instructions;

import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "showMessage")
public class ShowMessageInstr extends Instruction
{
    @XmlEnum
    enum MessageTypeEnum
    {
        @XmlEnumValue(value = "text")
        TEXT,
        @XmlEnumValue(value = "expression")
        EXPRESSION
    }

    @XmlAttribute(name="type")
    private MessageTypeEnum type = MessageTypeEnum.TEXT; // default type

    private Boolean carriageReturn = true;
    @XmlAttribute(name = "carriageReturn")
    public Boolean getCarriageReturn() { return carriageReturn; }
    public void setCarriageReturn(Boolean carriageReturn) { this.carriageReturn = carriageReturn; }

    private Boolean beginWithCap = false;
    @XmlAttribute(name = "beginWithCap")
    public Boolean getBeginWithCap() { return beginWithCap; }
    public void setBeginWithCap(Boolean beginWithCap) { this.beginWithCap = beginWithCap; }

    private String message;
    @XmlAttribute(name="message")
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString()
    {
        return "Вывести сообщение: " + getMessage();
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        String message = null;

        switch (type)
        {
            case TEXT:
                message = getMessage();
                break;
            case EXPRESSION:
                message = ExpressionCalculator.calculate(runningContext, getMessage()).toString();
                break;
        }

        if(beginWithCap)
        {
            message = CommonUtils.uppercaseFirstLetter(message);
        }

        if(carriageReturn)
        {
            virtualMachine.getEngine().outTextLn(message);
        }
        else
        {
            virtualMachine.getEngine().outText(message);
        }
    }

    public static String getTitle()
    {
        return "Вывести сообщение";
    }
}
