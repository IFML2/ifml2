package ifml2.vm.values;

import ifml2.om.IFMLObject;

public class ObjectValue extends Value
{
    public final IFMLObject value;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ObjectValue))
        {
            return false;
        }

        ObjectValue that = (ObjectValue) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public String toString()
    {
        return value.toString();
    }

    public ObjectValue(IFMLObject value)
    {
        this.value = value;
    }

    @Override
    public String getTypeName()
    {
        return "объект";
    }

    @Override
    public String toLiteral()
    {
        return value.getId();
    }

    public IFMLObject getValue()
    {
        return value;
    }
}
