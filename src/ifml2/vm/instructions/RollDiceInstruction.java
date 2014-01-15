package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;
import ifml2.vm.values.NumberValue;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Random;

@XmlRootElement(name = "rollDice")
public class RollDiceInstruction extends Instruction
{
    private int fromNumber;
    private int toNumber;
    private String varName;

    public static String getTitle()
    {
        return "Бросить кость";
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        Random rnd = new Random();
        int diff = toNumber - fromNumber;
        int dice = rnd.nextInt(diff + 1) + fromNumber;

        runningContext.setContextVariable(varName, new NumberValue(dice));
    }

    @Override
    public String toString()
    {
        return "Бросить кость " +
               fromNumber +
               " - " + toNumber +
               " и сохранить выпавшую грань в " + varName;
    }

    @XmlAttribute(name = "from")
    public void setFromNumber(int fromNumber)
    {
        this.fromNumber = fromNumber;
    }

    @XmlAttribute(name = "to")
    public void setToNumber(int toNumber)
    {
        this.toNumber = toNumber;
    }

    @XmlAttribute(name = "var")
    public void setVarName(String varName)
    {
        this.varName = varName;
    }

    public int getFromNumber()
    {
        return fromNumber;
    }

    public int getToNumber()
    {
        return toNumber;
    }

    public String getVarName()
    {
        return varName;
    }
}
