package ifml2.vm.values;

import ifml2.vm.IFML2VMException;
import ifml2.vm.SymbolResolver;

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

    public Value resolve(SymbolResolver symbolResolver) throws IFML2VMException {
        return symbolResolver.resolveSymbol(value.trim());
    }
}
