package ifml2.vm.values;

import ifml2.om.IFMLObject;

public class ObjectValue extends Value<IFMLObject> {

    public static final String LITERAL = "объект";

    public ObjectValue(IFMLObject value) {
        super(value);
    }

    @Override
    public String getTypeName() {
        return LITERAL;
    }

    @Override
    public String toLiteral() {
        return value.getId();
    }
}
