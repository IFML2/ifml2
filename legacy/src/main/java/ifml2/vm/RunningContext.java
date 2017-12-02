package ifml2.vm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ifml2.IFML2Exception;
import ifml2.om.Attribute;
import ifml2.om.IFMLObject;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.RoleDefinition;
import ifml2.vm.values.EmptyValue;
import ifml2.vm.values.Value;

public class RunningContext implements SymbolResolver {
    private HashMap<String, Variable> loweredLocalVariablesMap = new HashMap<String, Variable>();
    private VirtualMachine virtualMachine = null;
    private Value returnValue;
    private IFMLObject defaultObject;
    private Procedure contextProcedure; // procedure for searching procedure vars

    private RunningContext(VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    /**
     * Создание нового пустого контекста.
     */
    public static RunningContext CreateNewContext(@NotNull VirtualMachine virtualMachine) {
        return new RunningContext(virtualMachine);
    }

    /**
     * Создание нового пустого контекста для процедуры.
     */
    public static RunningContext CreateCallContext(@NotNull VirtualMachine virtualMachine,
            @NotNull Procedure contextProcedure, @Nullable List<Variable> parameters) {
        RunningContext runningContext = new RunningContext(virtualMachine);
        runningContext.contextProcedure = contextProcedure;

        // fill parameters
        if (parameters != null) {
            for (Variable parameter : parameters) {
                String name = parameter.getName();
                if (name != null) {
                    String loweredName = name.toLowerCase();
                    runningContext.loweredLocalVariablesMap.put(loweredName, parameter);
                }
            }
        }

        // fill not set parameters as EmptyValue
        for (Parameter parameter : contextProcedure.getParameters()) {
            if (runningContext.searchLocalVariable(parameter.getName()) == null) {
                runningContext.writeLocalVariable(new Variable(parameter.getName(), new EmptyValue()));
            }
        }

        return runningContext;
    }

    /**
     * Создание вложенного контекста (копирование ссылок на локальные переменные)
     */
    public static RunningContext CreateNestedContext(@NotNull RunningContext parentRunningContext) {
        RunningContext runningContext = new RunningContext(parentRunningContext.virtualMachine);

        // copy local variables
        Collection<Variable> localVariables = parentRunningContext.loweredLocalVariablesMap.values();
        for (Variable variable : localVariables) {
            runningContext.writeLocalVariable(variable);
        }

        return runningContext;
    }

    public Value resolveSymbol(@NotNull String symbol) throws IFML2VMException {
        String loweredSymbol = symbol.toLowerCase();

        // check context variables
        Variable variable;

        try {
            variable = searchVariable(loweredSymbol);
        } catch (IFML2Exception e) {
            throw new IFML2VMException(e, "{0}\n  во время поиска символа \"{1}\" в контексте", e.getMessage(), symbol);
        }

        if (variable != null) {
            Value value = variable.getValue();
            if (value != null) {
                return value;
            }
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

    @Contract("null -> null")
    private Variable searchLocalVariable(String name) {
        if (name == null) {
            return null;
        }

        String loweredName = name.toLowerCase();

        if (loweredLocalVariablesMap.containsKey(loweredName)) {
            return loweredLocalVariablesMap.get(loweredName);
        }

        return null;
    }

    /**
     * Searches for variable by name in scope (local, then procedure, then global)
     * and set is by value.
     *
     * @param name
     *            name of variable.
     * @param value
     *            value for setting.
     */
    public void writeVariable(@NotNull String name, Value value) throws IFML2Exception {
        String loweredName = name.toLowerCase();

        // search local variable
        if (loweredLocalVariablesMap.containsKey(loweredName)) {
            Variable variable = loweredLocalVariablesMap.get(loweredName);
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
        loweredLocalVariablesMap.put(loweredName, new Variable(name, value));
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

    private void writeLocalVariable(@NotNull Variable variable) {
        Optional.ofNullable(variable.getName()).ifPresent(name -> loweredLocalVariablesMap.put(name.toLowerCase(), variable));
    }

    public void populateParameters(List<Variable> parameters) {
        if (parameters != null) {
            parameters.forEach(this::writeLocalVariable);
        }
    }
}
