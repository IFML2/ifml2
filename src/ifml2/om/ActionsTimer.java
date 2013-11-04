package ifml2.om;

import ifml2.vm.VirtualMachine;

public class ActionsTimer extends GameTimer
{
    private Integer quantity;

    public ActionsTimer(String name, Integer quantity)
    {
        super(name);
        this.quantity = quantity;
    }

    @Override
    public void start(VirtualMachine virtualMachine)
    {
        //todo quantity timer!
    }

    public Integer getQuantity()
    {
        return quantity;
    }
}
