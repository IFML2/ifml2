package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.RunningContext;
import ifml2.vm.values.Value;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;

@XmlRootElement(name = "return")
@IFML2Instruction(title = "Вернуть значение")
public class ReturnInstruction extends Instruction
{
    @XmlAttribute(name = "value")
    private String value;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Value returnValue = ExpressionCalculator.calculate(runningContext, value);
        runningContext.setReturnValue(returnValue);
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Вернуть значение выражения \"{0}\"", value);
    }
}
