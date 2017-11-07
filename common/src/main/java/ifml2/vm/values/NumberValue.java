package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

import static ifml2.vm.values.CompareResult.EQUAL;
import static ifml2.vm.values.CompareResult.LEFT_BIGGER;
import static ifml2.vm.values.CompareResult.RIGHT_BIGGER;
import static ifml2.vm.values.Operation.ADD;

public class NumberValue extends Value<Double> implements IAddableValue {

    public static final String LITERAL = "число";

    public NumberValue(double value) {
        super(value);
    }

    @Override
    public CompareResult compareTo(final Value rightValue) {
        if (rightValue instanceof NumberValue) {
            final int compareResult = Double.compare(value, ((NumberValue) rightValue).getValue());
            if (compareResult == 0) {
                return EQUAL;
            } else if (compareResult > 0) {
                return LEFT_BIGGER;
            } else {
                return RIGHT_BIGGER;
            }
        }

        return super.compareTo(rightValue);
    }

    @Override
    public String toString() {
        return toLiteral();
    }

    @Override
    public Value plus(Value rightValue) throws IFML2ExpressionException {
        if (rightValue instanceof NumberValue) {
            return new NumberValue(value + ((NumberValue) rightValue).getValue());
        } else if (rightValue instanceof TextValue) {
            return new TextValue(toLiteral() + ((TextValue) rightValue).getValue());
        }

        throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"", ADD,
                getTypeName(), rightValue.getTypeName());
    }

    @Override
    public String getTypeName() {
        return LITERAL;
    }

    @Override
    public String toLiteral() {
        Long roundedValue = Math.round(value);
        return value == roundedValue.doubleValue() ? roundedValue.toString() : value.toString();
    }
}
