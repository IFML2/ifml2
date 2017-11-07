package ifml2.vm.values;

import ifml2.IFMLEntity;

import static ifml2.vm.values.CompareResult.EQUAL;
import static ifml2.vm.values.CompareResult.NOT_APPLICABLE;
import static ifml2.vm.values.CompareResult.UNEQUAL;

public abstract class Value<T> extends IFMLEntity implements Cloneable {
    protected T value;

    public Value(T value) {
        this.value = value;
    }

    protected Value() {
    }

    public abstract String getTypeName();

    public abstract String toLiteral();

    public T getValue() {
        return value;
    }

    public CompareResult compareTo(Value rightValue) {
        if (getClass().equals(rightValue.getClass())) {
            // одинаковые классы сравниваем напрямую через equals
            return equals(rightValue) ? EQUAL : UNEQUAL;
        } else if (rightValue instanceof EmptyValue) {
            // если правое значение - пустота, то возвращаем равенство, если this тоже пустота
            return this instanceof EmptyValue ? EQUAL : UNEQUAL; // irish principle (double check) - cause it's overridden in EmptyValue
        }
        return NOT_APPLICABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Value)) {
            return false;
        }

        Value that = (Value) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }

    @Override
    public Value clone() throws CloneNotSupportedException {
        return (Value) super.clone();
    }
}
