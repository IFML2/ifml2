package ifml2.vm.values;

public class BooleanValue extends Value<Boolean> {

    public static final String LITERAL = "логика";
    public static final String TRUE = "да";
    public static final String FALSE = "нет";

    public BooleanValue(boolean value) {
        super(value);
    }

    @Override
    public String toString() {
        return toLiteral();
    }

    @Override
    public String getTypeName() {
        return LITERAL;
    }

    @Override
    public String toLiteral() {
        return value ? TRUE : FALSE;
    }
}
