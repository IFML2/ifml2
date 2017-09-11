package ifml2.vm;

import ifml2.vm.values.Value;

public class Variable implements Cloneable {
    protected String name;
    private Value value;

    public Variable(String name, Value value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return name + " = " + value;
    }

    @Override
    protected Variable clone() throws CloneNotSupportedException {
        return (Variable) super.clone();
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
