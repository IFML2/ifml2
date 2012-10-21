package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;

@XmlRootElement(name = "var")
public class SetVarInstruction extends Instruction
{
    @XmlAttribute(name = "name")
    private String name;
    
    @XmlAttribute(name = "value")
    private String value;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        runningContext.setContextVariable(name, ExpressionCalculator.calculate(runningContext,  value));
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Установить переменной {0} значение {1}", name,  value);
    }

    public static String getTitle()
    {
        return "Установить переменную";
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}
