package ifml2.vm.values;

import ifml2.SystemIdentifiers;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(boolean value) {
        super(value);
    }

    @Override
    public String toString() {
        return toLiteral();
    }

    @Override
    public String getTypeName() {
        return "логика";
    }

    @Override
    public String toLiteral() {
        return value ? SystemIdentifiers.TRUE_BOOL_LITERAL : SystemIdentifiers.FALSE_BOOL_LITERAL;
    }
}
