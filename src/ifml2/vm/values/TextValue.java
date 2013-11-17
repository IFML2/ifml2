package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

public class TextValue extends Value implements IAddableValue
{
    private final String value;

    public TextValue(String value)
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
        if (!(o instanceof TextValue))
        {
            return false;
        }

        TextValue that = (TextValue) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public Value add(Value rightValue) throws IFML2ExpressionException
    {
        return new TextValue(value + rightValue.toString());
    }

    @Override
    public String getTypeName()
    {
        return "текст";
    }

    @Override
    public String toLiteral()
    {
        return '\'' +  value + '\'';
    }

    public String getValue()
    {
        return value;
    }
}
