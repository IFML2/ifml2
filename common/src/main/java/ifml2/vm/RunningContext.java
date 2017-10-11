package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.om.Attribute;
import ifml2.om.IFMLObject;
import ifml2.om.Procedure;
import ifml2.om.RoleDefinition;
import ifml2.vm.values.EmptyValue;
import ifml2.vm.values.Value;

import java.util.Collection;
import java.util.List;

public class RunningContext implements SymbolResolver {

    private VariableMap variables = new VariableMapImpl();

    private Value returnValue;
    private IFMLObject defaultObject;

    private final VirtualMachine virtualMachine;
    private final Procedure contextProcedure; // procedure for searching procedure vars

    public RunningContext(VirtualMachine virtualMachine) {
        this(virtualMachine, null);
    }

    public RunningContext(VirtualMachine virtualMachine, Procedure contextProcedure) {
        this.virtualMachine = virtualMachine;
        this.contextProcedure = contextProcedure;
    }

    public void putVariable(Variable variable) {
        variables.put(variable);
    }

    public Collection<Variable> getVariables() {
        return variables.getVariables();
    }

    public boolean isLocalVaribleExists(final String name) {
        return this.searchLocalVariable(name) != null;
    }

    public boolean isLocalVariableNotExists(final String name) {
        return !isLocalVaribleExists(name);
    }


    public void writeEmptyLocalVariable(final String name) {
        writeLocalVariable(new Variable(name, new EmptyValue()));
    }

    public Value resolveSymbol(String symbol) throws IFML2VMException {
        String loweredSymbol = symbol.toLowerCase();

        // check context variables
        Variable variable;

        try {
            variable = searchVariable(loweredSymbol);
        } catch (IFML2Exception e) {
            throw new IFML2VMException(e, "{0}\n  во время поиска символа \"{1}\" в контексте", e.getMessage(), symbol);
        }

        if (variable != null && variable.getValue() != null) {
            return variable.getValue();
        }

        // check default object
        if (defaultObject != null) {
            Value value = defaultObject.tryGetMemberValue(loweredSymbol, this);
            if (value != null) {
                return value;
            }
        }

        // check VM symbols
        return virtualMachine.resolveSymbol(symbol);
    }

    @Override
    public List<Attribute> getAttributeList() {
        return virtualMachine.getStory().getAllAttributes();
    }

    @Override
    public List<RoleDefinition> getRoleDefinitionList() {
        return virtualMachine.getStory().getAllRoleDefinitions();
    }

    private Variable searchVariable(String name) throws IFML2Exception {
        // search local
        Variable variable = searchLocalVariable(name);
        if (variable != null) {
            return variable;
        }

        // search procedure
        if (contextProcedure != null) {
            variable = contextProcedure.searchProcedureVariable(name, this);
            if (variable != null) {
                return variable;
            }
        }

        // search global
        return virtualMachine.searchGlobalVariable(name);
    }

    private Variable searchLocalVariable(String name) {
        return name == null ? null : variables.get(name);
    }

    /**
     * Searches for variable by name in scope (local, then procedure, then global) and set is by value.
     *
     * @param name  name of variable.
     * @param value value for setting.
     */
    public void writeVariable(String name, Value value) throws IFML2Exception {
        String loweredName = name.toLowerCase();

        // search local variable
        if (variables.containsVariable(name)) {
            Variable variable = variables.get(name);
            variable.setValue(value);
            return;
        }

        // search procedure variable
        if (contextProcedure != null) {
            Variable variable = contextProcedure.searchProcedureVariable(loweredName, this);
            if (variable != null) {
                variable.setValue(value);
                return;
            }
        }

        // search global variable
        Variable variable = virtualMachine.searchGlobalVariable(loweredName);
        if (variable != null) {
            variable.setValue(value);
            return;
        }

        // write local variable
        variables.put(new Variable(name, value));
    }

    public void setDefaultObject(IFMLObject defaultObject) {
        this.defaultObject = defaultObject;
    }

    public Value getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Value returnValue) {
        this.returnValue = returnValue;
    }

    public void writeLocalVariable(Variable variable) {
        if (variable.getName()!= null) {
            variables.put(variable);
        }
    }

    public void populateParameters(List<Variable> parameters) {
        if (parameters != null) {
            parameters.forEach(this::writeLocalVariable);
        }
    }

}
