package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created with IntelliJ IDEA.
 * User: realsonic
 * Date: 04.11.13
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class StartTimerInstruction extends Instruction
{
    private String name;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        virtualMachine.startTimer(name);
    }

    @XmlAttribute(name = "name")
    public void setName(String name)
    {
        this.name = name;
    }
}
