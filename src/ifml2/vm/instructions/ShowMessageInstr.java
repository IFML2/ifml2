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
@IFML2Instruction(title = "Вывести сообщение")
public class ShowMessageInstr extends Instruction
{
    private MessageTypeEnum type = MessageTypeEnum.TEXT; // default type
    private Boolean carriageReturn = true;
    private Boolean beginWithCap = false;
    private String messageExpr;

    public MessageTypeEnum getType()
    {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setType(MessageTypeEnum type)
    {
        this.type = type;
    }

    public Boolean getCarriageReturn()
    {
        return carriageReturn;
    }

    @XmlAttribute(name = "carriageReturn")
    public void setCarriageReturn(Boolean carriageReturn)
    {
        this.carriageReturn = carriageReturn;
    }

    public Boolean getBeginWithCap()
    {
        return beginWithCap;
    }

    @XmlAttribute(name = "beginWithCap")
    public void setBeginWithCap(Boolean beginWithCap)
    {
        this.beginWithCap = beginWithCap;
    }

    public String getMessageExpr()
    {
        return messageExpr;
    }

    @XmlAttribute(name = "message")
    public void setMessageExpr(String messageExpr)
    {
        this.messageExpr = messageExpr;
    }

    @Override
    public String toString()
    {
        return "Вывести сообщение: " + messageExpr;
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        String message = null;

        switch (type)
        {
            case TEXT:
                message = messageExpr;
                break;
            case EXPRESSION:
                message = ExpressionCalculator.calculate(runningContext, messageExpr).toString();
                break;
        }

        if (beginWithCap)
        {
            message = CommonUtils.uppercaseFirstLetter(message);
        }

        if (carriageReturn)
        {
            virtualMachine.outTextLn(message);
        }
        else
        {
            virtualMachine.outText(message);
        }
    }

    @XmlEnum
    public enum MessageTypeEnum
    {
        @XmlEnumValue(value = "text")
        TEXT,
        @XmlEnumValue(value = "expression")
        EXPRESSION
    }
}
