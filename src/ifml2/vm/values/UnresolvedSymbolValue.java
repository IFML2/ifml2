package ifml2.vm.values;

public class UnresolvedSymbolValue extends Value
{
    public final String value;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof UnresolvedSymbolValue))
        {
            return false;
        }

        UnresolvedSymbolValue that = (UnresolvedSymbolValue) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    public UnresolvedSymbolValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public String getTypeName()
    {
        return "идентификатор";
    }
}
