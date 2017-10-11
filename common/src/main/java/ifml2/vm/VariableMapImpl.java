package ifml2.vm;

import ifml2.vm.values.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VariableMapImpl implements VariableMap {

    private final Map<String, Variable> variables = new HashMap<String, Variable>();

    @Override
    public void clear() {
        variables.clear();
    }

    @Override
    public void put(Variable variable) {
        variables.put(variable.getName().toLowerCase(), variable);
    }

    @Override
    public Collection<Variable> getVariables() {
        return variables.values();
    }

    @Override
    public boolean containsVariable(String name) {
        return variables.containsKey(name.toLowerCase());
    }

    @Override
    public Variable get(String name) {
        return variables.getOrDefault(name.toLowerCase(), null);
    }

    @Override
    public Map<String, Value> toValues() {
        Map<String, Value> valueMap = new HashMap<>();
        variables.forEach((key, value) -> valueMap.put(key, value.getValue()));
        return valueMap;
    }

    @Override
    public String toString() {
        return "VariableMap(" + variables.toString() + ")";
    }

}
