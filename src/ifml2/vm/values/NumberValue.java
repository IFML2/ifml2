package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

public class NumberValue extends Value implements IAddableValue
{
    private final Double value;

    public NumberValue(double value)
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
        if (!(o instanceof NumberValue))
        {
            return false;
        }

        NumberValue that = (NumberValue) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);
    }

    @Override
    public String toString()
    {
        Long roundedValue = Math.round(value);
        return value == roundedValue.doubleValue() ? roundedValue.toString() : value.toString();
    }

    @Override
    public Value add(Value rightValue) throws IFML2ExpressionException
    {
        if(rightValue instanceof NumberValue)
        {
            return new NumberValue(value + ((NumberValue) rightValue).value);
        }
        
        if(rightValue instanceof TextValue)
        {
            return new TextValue(toString() + ((TextValue)rightValue).getValue());
        }

        throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"",
                OperationEnum.ADD, getTypeName(), rightValue.getTypeName());
    }

    @Override
    public String getTypeName()
    {
        return "число";
    }
}
