package ifml2.vm.values;

import ifml2.IFMLEntity;

import java.util.List;

public class CollectionValue extends Value<List<? extends IFMLEntity>> {

    public static final String LITERAL = "коллекция";

    public CollectionValue(List<? extends IFMLEntity> value) {
        super(value);
    }

    @Override
    public String toString() {
        return "CollectionValue";
    }

    @Override
    public String getTypeName() {
        return LITERAL;
    }

    @Override
    public String toLiteral() {
        throw new UnsupportedOperationException("Not Implemented Yet"); //todo toLiteral
    }
}
