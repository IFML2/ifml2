package ifml2.vm.values;

import static ifml2.vm.values.Value.CompareResult.EQUAL;
import static ifml2.vm.values.Value.CompareResult.UNEQUAL;

/**
 * Empty value
 */
public class EmptyValue extends Value {

    @Override
    public String getTypeName() {
        return "пусто";
    }

    @Override
    public String toString() {
        return toLiteral();
    }

    @Override
    public String toLiteral() {
        return "пусто";
    }

    @Override
    public CompareResult compareTo(/*@NotNull*/ Value rightValue) {
        return rightValue instanceof EmptyValue ? EQUAL : UNEQUAL;
    }
}
