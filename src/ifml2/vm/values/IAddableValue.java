package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

public interface IAddableValue
{
    public abstract Value add(Value rightValue) throws IFML2ExpressionException;
}
