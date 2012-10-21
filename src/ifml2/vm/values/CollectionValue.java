package ifml2.vm.values;

import java.util.List;

public class CollectionValue extends Value
{
    public final List value;

    public CollectionValue(List value)
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
        if (!(o instanceof CollectionValue))
        {
            return false;
        }

        CollectionValue that = (CollectionValue) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public String toString()
    {
        return value.toString();
    }

    @Override
    public String getTypeName()
    {
        return "коллекция";
    }
}
