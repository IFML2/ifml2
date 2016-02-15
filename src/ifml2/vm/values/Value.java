package ifml2.vm.values;

import ifml2.IFMLEntity;
import org.jetbrains.annotations.NotNull;

import static ifml2.vm.values.Value.CompareResult.*;

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

    public CompareResult compareTo(@NotNull Value rightValue)
    {
        if(getClass().equals(rightValue.getClass()))
        {
            // одинаковые классы сравниваем напрямую через equals
            return equals(rightValue) ? EQUAL : UNEQUAL;
        }
        else if(rightValue instanceof EmptyValue)
        {
            // если правое значение - пустота, то возвращаем равенство, если this тоже пустота
            return this instanceof EmptyValue ? EQUAL : UNEQUAL;
        }
        return NOT_APPLICABLE;
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

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public String toString()
    {
        return value != null ? value.toString() : "";
    }

    @Override
    public Value clone() throws CloneNotSupportedException
    {
        return (Value) super.clone();
    }

    public enum Operation
    {
        ADD("сложение");

        final String caption;

        Operation(String caption)
        {
            this.caption = caption;
        }

        @Override
        public String toString()
        {
            return caption;
        }
    }

    public enum CompareResult
    {
        EQUAL,
        UNEQUAL,
        LEFT_BIGGER,
        RIGHT_BIGGER,
        NOT_APPLICABLE
    }
}
