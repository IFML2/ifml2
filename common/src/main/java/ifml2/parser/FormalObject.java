package ifml2.parser;

import ifml2.om.IFMLObject;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;

public class FormalObject extends FormalElement {

    private IFMLObject object;

    public FormalObject(final IFMLObject object, final String parameterName) {
        super(parameterName);
        this.object = object;
    }

    public IFMLObject getObject() {
        return object;
    }

    @Override public Value getValue() {
        return new ObjectValue(object);
    }

    @Override public String toString() {
        return object.toString();
    }

}
