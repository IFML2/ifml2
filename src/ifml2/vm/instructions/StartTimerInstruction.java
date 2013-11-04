package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.IFML2ObjectNotFoundException;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;

@XmlRootElement(name = "startTimer")
public class StartTimerInstruction extends Instruction
{
    private String name;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        try
        {
            virtualMachine.startTimer(name);
        }
        catch (IFML2ObjectNotFoundException e)
        {
            throw new IFML2VMException(e, e.getMessage());
        }
    }

    @XmlAttribute(name = "name")
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Запустить таймер \"{0}\"", name);
    }
}
