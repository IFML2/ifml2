package ifml2.vm.values;

import ifml2.vm.IFML2ExpressionException;

public class TextValue extends Value<String> implements IAddableValue {

    public static final String LITERAL = "текст";

    public TextValue(String value) {
        super(value);
    }

    @Override
    public TextValue plus(Value rightValue) throws IFML2ExpressionException {
        return new TextValue(value + rightValue.toString());
    }

    @Override
    public String getTypeName() {
        return LITERAL;
    }

    @Override
    public String toLiteral() {
        return '\'' + value + '\'';
    }
}
