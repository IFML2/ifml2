package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

import static ifml2.vm.values.Value.Operation.ADD;

public class NumberValue extends Value<Double> implements IAddableValue
{
    public NumberValue(double value)
    {
        super(value);
    }

    @Override
    public String toString()
    {
        return toLiteral();
    }

    @Override
    public Value add(Value rightValue) throws IFML2ExpressionException
    {
        if (rightValue instanceof NumberValue)
        {
            return new NumberValue(value + ((NumberValue) rightValue).getValue());
        }

        if (rightValue instanceof TextValue)
        {
            return new TextValue(toLiteral() + ((TextValue) rightValue).getValue());
        }

        throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"", ADD,
                                           getTypeName(), rightValue.getTypeName());
    }

    @Override
    public String getTypeName()
    {
        return "число";
    }

    @Override
    public String toLiteral()
    {
        Long roundedValue = Math.round(value);
        return value == roundedValue.doubleValue() ? roundedValue.toString() : value.toString();
    }
}
