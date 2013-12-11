package ifml2.vm.values;

import ifml2.SystemIdentifiers;

public class BooleanValue extends Value
{
    public final boolean value;

    public boolean getValue()
    {
        return value;
    }

    public BooleanValue(boolean value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BooleanValue))
        {
            return false;
        }

        BooleanValue that = (BooleanValue) o;

        return value == that.value;
    }

    @Override
    public String toString()
    {
        return toLiteral();
    }

    @Override
    public String getTypeName()
    {
        return "логика";
    }

    @Override
    public String toLiteral()
    {
        return value ? SystemIdentifiers.TRUE_BOOL_LITERAL : SystemIdentifiers.FALSE_BOOL_LITERAL;
    }
}
