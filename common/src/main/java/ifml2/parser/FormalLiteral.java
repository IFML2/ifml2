package ifml2.parser;

import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;

public class FormalLiteral extends FormalElement {

    private String literal;
    private String parameterName;

    public FormalLiteral(final String literal, final String parameterName) {
        super(parameterName);
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    @Override public Value getValue() {
        return new TextValue(literal);
    }

    @Override public String toString() {
        return literal;
    }

}
