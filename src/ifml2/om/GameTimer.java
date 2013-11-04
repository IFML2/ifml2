package ifml2.om;

import ifml2.vm.VirtualMachine;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "timer")
public abstract class GameTimer extends InstructionList
{
    protected VirtualMachine virtualMachine;
    private String name = "";

    public GameTimer(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public abstract void start(VirtualMachine virtualMachine);
}
