package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.InstructionList;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "if")
public class IfInstruction extends Instruction
{
    @XmlAttribute(name = "condition")
    private String conditionExpression;

    @XmlElement(name = "then")
    private final InstructionList thenInstructions = new InstructionList();

    @XmlElement(name = "else")
    private final InstructionList elseInstructions = new InstructionList();
    
    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        boolean conditionValue = getBooleanFromExpression(conditionExpression, runningContext, getTitle(), "Условие");

        if(conditionValue)
        {
            virtualMachine.runInstructionList(thenInstructions, runningContext, true, conditionValue);
        }
        else
        {
            virtualMachine.runInstructionList(elseInstructions, runningContext, true, conditionValue);
        }
    }

    @Override
    public String toString()
    {
        return "Проверка условия: " + conditionExpression;
    }

    public static String getTitle()
    {
        return "Проверка условия";
    }
}
