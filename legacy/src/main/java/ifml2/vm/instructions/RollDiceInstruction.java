package ifml2.vm.instructions;

import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;
import ifml2.vm.values.NumberValue;

@XmlRootElement(name = "rollDice")
@IFML2Instruction(title = "Бросить кость")
public class RollDiceInstruction extends Instruction {
    private int fromNumber;
    private int toNumber;
    private String varName;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception {
        Random rnd = new Random();
        int diff = toNumber - fromNumber;
        int dice = rnd.nextInt(diff + 1) + fromNumber;

        runningContext.writeVariable(varName, new NumberValue(dice));
    }

    @Override
    public String toString() {
        return "Бросить кость " + fromNumber + " - " + toNumber + " и сохранить выпавшую грань в " + varName;
    }

    public int getFromNumber() {
        return fromNumber;
    }

    @XmlAttribute(name = "from")
    public void setFromNumber(int fromNumber) {
        this.fromNumber = fromNumber;
    }

    public int getToNumber() {
        return toNumber;
    }

    @XmlAttribute(name = "to")
    public void setToNumber(int toNumber) {
        this.toNumber = toNumber;
    }

    public String getVarName() {
        return varName;
    }

    @XmlAttribute(name = "var")
    public void setVarName(String varName) {
        this.varName = varName;
    }
}
