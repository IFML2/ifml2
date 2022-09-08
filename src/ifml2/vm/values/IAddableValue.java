package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

public interface IAddableValue {
    Value add(Value rightValue) throws IFML2ExpressionException;
}
