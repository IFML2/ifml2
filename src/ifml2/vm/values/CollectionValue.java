package ifml2.vm.values;

import ifml2.IFMLEntity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class CollectionValue extends Value<List<? extends IFMLEntity>>
{
    public CollectionValue(List<? extends IFMLEntity> value)
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
