package ifml2.vm;

import ifml2.vm.values.Value;

import java.util.Collection;
import java.util.Map;

public interface VariableMap {

    void clear();

    void put(Variable variable);

    Collection<Variable> getVariables();

    boolean containsVariable(String name);

    Variable get(String name);

    Map<String, Value> toValues();

}
