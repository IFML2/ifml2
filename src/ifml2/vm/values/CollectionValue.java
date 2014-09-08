package ifml2.vm.values;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class CollectionValue extends Value<List<?>>
{
    public CollectionValue(List<?> value)
    {
        super(value);
    }

    @Override
    public String getTypeName()
    {
        return "коллекция";
    }

    @Override
    public String toLiteral()
    {
        throw new NotImplementedException(); //todo toLiteral
    }
}
