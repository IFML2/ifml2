package ifml2.vm;

import ifml2.vm.values.Value;

public class Variable
{
    public final String name;
    public final Value value;

    public Variable(String name, Value value)
    {
        this.name = name;
        this.value = value;
    }

    public enum VariableScope
    {
        LOCAL,
        PROCEDURE,
        GLOBAL
    }
}
