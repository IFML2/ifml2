package ifml2.parser;

import ifml2.om.IFMLObject;
import ifml2.vm.values.Value;

public abstract class FormalElement {

    public IFMLObject object = null;

    private String parameterName;

    public FormalElement(final String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    public abstract Value getValue();

    @Override
    public String toString() {
        throw new RuntimeException("Неверное значение типа формального элемента в FormalElement.toString()!");
    }

}
