package ifml2.vm;

import ifml2.om.IFMLObject;
import ifml2.om.Story;
import ifml2.parser.FormalElement;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;

import java.util.HashMap;
import java.util.List;

public class RunningContext
{
    private VirtualMachine virtualMachine = null;

    private final HashMap<Variable.VariableScope, HashMap<String, Variable>> variables = new HashMap<Variable.VariableScope, HashMap<String, Variable>>()
    {
        {
            put(Variable.VariableScope.LOCAL, new HashMap<String, Variable>());
            put(Variable.VariableScope.PROCEDURE, new HashMap<String, Variable>());
            put(Variable.VariableScope.GLOBAL, new HashMap<String, Variable>());
        }
    }; //todo create properties for all the scopes
    private Value returnValue;
    private IFMLObject defaultObject;

    public RunningContext(List<FormalElement> parameters, VirtualMachine virtualMachine) throws IFML2VMException
    {
        this(virtualMachine);

        for(FormalElement parameter : parameters)
        {
            if(parameter.parameterName == null)
            {
                continue;
            }

            Variable variable;

            switch (parameter.type)
            {
                case LITERAL:
                    variable = new Variable(parameter.parameterName, new TextValue(parameter.literal));
                    break;
                case OBJECT:
                    variable = new Variable(parameter.parameterName, new ObjectValue(parameter.object));
                    break;

                default:
                    throw new IFML2VMException("Неизвестный тип параметра");
            }

            variables.get(Variable.VariableScope.LOCAL).put(variable.name.toLowerCase(), variable);
        }
    }

    public RunningContext(VirtualMachine virtualMachine)
    {
        this.virtualMachine = virtualMachine;
    }

    public RunningContext(RunningContext runningContext)
    {
        this(runningContext.virtualMachine);
        variables.putAll(runningContext.variables);
        variables.get(Variable.VariableScope.GLOBAL).putAll(runningContext.variables.get(Variable.VariableScope.GLOBAL));
        variables.get(Variable.VariableScope.PROCEDURE).putAll(runningContext.variables.get(Variable.VariableScope.PROCEDURE));
        variables.get(Variable.VariableScope.LOCAL).putAll(runningContext.variables.get(Variable.VariableScope.LOCAL));
        defaultObject = runningContext.defaultObject;
        returnValue = runningContext.returnValue;
    }

    public Value resolveSymbol(String symbol) throws IFML2VMException 
    {
        String loweredSymbol = symbol.toLowerCase();

        // check context variables
        Value value = getContextVariable(loweredSymbol);
        if(value != null)
        {
            return value;
        }

        // check default object
        if(defaultObject != null)
        {
            value = defaultObject.tryGetMemberValue(loweredSymbol, this);
            if(value != null)
            {
                return value;
            }
        }

        // check VM symbols
        return virtualMachine.resolveSymbol(symbol);
    }

    private Value getContextVariable(String name)
    {
        Value value = getVariable(Variable.VariableScope.LOCAL, name);
        if(value != null)
        {
            return value;
        }

        value = getVariable(Variable.VariableScope.PROCEDURE, name);
        if(value != null)
        {
            return value;
        }

        return getVariable(Variable.VariableScope.GLOBAL, name);
    }

    public Value getVariable(Variable.VariableScope scope, String name)
    {
        if(variables.get(scope).containsKey(name.toLowerCase()))
        {
            return variables.get(scope).get(name.toLowerCase()).value;
        }
        return null;
    }

    public void setVariable(Variable.VariableScope scope, String name, Value value)
    {
        variables.get(scope).put(name.toLowerCase(), new Variable(name, value));
    }

    public void deleteVariable(Variable.VariableScope scope, String name)
    {
        String loweredName = name.toLowerCase();
        HashMap<String, Variable> variableHashMap = variables.get(scope);
        if(variableHashMap.containsKey(loweredName))
        {
            variableHashMap.remove(loweredName);
        }
    }

    public Story getStory()
    {
        return virtualMachine.getEngine().getStory();
    }

    public void setContextVariable(String name, Value value)
    {
        String loweredName = name.toLowerCase();
        if(variables.get(Variable.VariableScope.LOCAL).containsKey(loweredName))
        {
            setVariable(Variable.VariableScope.LOCAL, name, value);
        }
        else if(variables.get(Variable.VariableScope.PROCEDURE).containsKey(loweredName))
        {
            setVariable(Variable.VariableScope.PROCEDURE, name, value);
        }
        else if(variables.get(Variable.VariableScope.GLOBAL).containsKey(loweredName))
        {
            setVariable(Variable.VariableScope.GLOBAL, name, value);
        }
        else
        {
            setVariable(Variable.VariableScope.LOCAL, name, value);
        }
    }

    public void setReturnValue(Value returnValue)
    {
        this.returnValue = returnValue;
    }

    public void setDefaultObject(IFMLObject defaultObject)
    {
        this.defaultObject = defaultObject;
    }

    public Value getReturnValue()
    {
        return returnValue;
    }

    /*public IFMLObject getObjectByName(String name)
    {
        Story story = getStory();
        String loweredName = name.toLowerCase();
        if(story.getObjectsHeap().containsKey(loweredName))
        {
            return story.getObjectsHeap().get(loweredName);
        }
        else
        {
            return null;
        }
    }*/
}
