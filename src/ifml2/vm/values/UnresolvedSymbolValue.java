package ifml2.vm.values;

public class UnresolvedSymbolValue extends Value<String>
{
    public UnresolvedSymbolValue(String value)
    {
        super(value);
    }

    @Override
    public String getTypeName()
    {
        return "идентификатор";
    }

    @Override
    public String toLiteral()
    {
        return value;
    }
}
