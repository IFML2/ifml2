package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.InstructionList;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "if")
@IFML2Instruction(title = "Проверить условие")
public class IfInstruction extends Instruction {
    private String condition;
    private InstructionList thenInstructions = new InstructionList();
    private InstructionList elseInstructions = new InstructionList();

    public String getCondition() {
        return condition;
    }

    @XmlAttribute(name = "condition")
    public void setCondition(String condition) {
        this.condition = condition;
    }

    public InstructionList getThenInstructions() {
        return thenInstructions;
    }

    @XmlElement(name = "then")
    public void setThenInstructions(InstructionList thenInstructions) {
        this.thenInstructions = thenInstructions;
    }

    public InstructionList getElseInstructions() {
        return elseInstructions;
    }

    @XmlElement(name = "else")
    public void setElseInstructions(InstructionList elseInstructions) {
        this.elseInstructions = elseInstructions;
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception {
        boolean conditionValue = getBooleanFromExpression(condition, runningContext, getTitle(), "Условие");

        virtualMachine.runInstructionList(conditionValue ? thenInstructions : elseInstructions, runningContext);
    }

    @Override
    public String toString() {
        return "Проверить условие: " + condition;
    }
}
