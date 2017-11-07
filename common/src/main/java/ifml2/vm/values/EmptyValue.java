package ifml2.vm.values;

import static ifml2.vm.values.CompareResult.EQUAL;
import static ifml2.vm.values.CompareResult.UNEQUAL;

public class EmptyValue extends Value {

    public static final String EMPTY = "пусто";
    public static final String LITERAL = EMPTY;

    @Override
    public String getTypeName() {
        return LITERAL;
    }

    @Override
    public String toString() {
        return toLiteral();
    }

    @Override
    public String toLiteral() {
        return LITERAL;
    }

    @Override
    public CompareResult compareTo(Value rightValue) {
        return rightValue instanceof EmptyValue ? EQUAL : UNEQUAL;
    }
}
