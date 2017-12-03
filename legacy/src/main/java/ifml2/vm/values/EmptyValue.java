package ifml2.vm.values;

import static ifml2.vm.values.Value.CompareResult.EQUAL;
import static ifml2.vm.values.Value.CompareResult.UNEQUAL;

import org.jetbrains.annotations.NotNull;

/**
 * Empty value
 */
public class EmptyValue extends Value {

    public static final String LITERAL = "пусто";

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
    public CompareResult compareTo(@NotNull Value rightValue) {
        return rightValue instanceof EmptyValue ? EQUAL : UNEQUAL;
    }
}
