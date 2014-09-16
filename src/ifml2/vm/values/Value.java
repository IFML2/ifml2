package ifml2.vm.values;

import ifml2.IFMLEntity;

public abstract class Value<T> extends IFMLEntity implements Cloneable
{
    protected T value;

    public Value(T value)
    {
        this.value = value;
    }

    protected Value()
    {
    }

    public abstract String getTypeName();

    public abstract String toLiteral();

    public T getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Value))
        {
            return false;
        }

        Value that = (Value) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public String toString()
    {
        return value.toString();
    }

    @Override
    public Value clone() throws CloneNotSupportedException
    {
        return (Value) super.clone();
    }

    public enum OperationEnum
    {
        ADD("сложение");

        final String caption;

        OperationEnum(String caption)
        {
            this.caption = caption;
        }

        @Override
        public String toString()
        {
            return caption;
        }
    }
}
