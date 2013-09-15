package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.SystemIdentifiers;
import ifml2.engine.Engine;
import ifml2.om.*;
import ifml2.parser.FormalElement;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.Value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ifml2.om.Procedure.SystemProcedureEnum;

public class VirtualMachine
{
    private final HashMap<String, Value> systemConstants = new HashMap<String, Value>()
    {
        {
            put(SystemIdentifiers.TRUE_BOOL_LITERAL, new BooleanValue(true));
            put(SystemIdentifiers.FALSE_BOOL_LITERAL, new BooleanValue(false));
        }
    };
    private final HashMap<Procedure.SystemProcedureEnum, Procedure> inheritedSystemProcedures = new HashMap<Procedure.SystemProcedureEnum, Procedure>()
    {
        @Override
        public Procedure get(Object key)
        {
            // lazy initialization
            if (!containsKey(key))
            {
                Procedure inheritor = engine.getStory().getSystemInheritorProcedure((SystemProcedureEnum) key);
                put((SystemProcedureEnum) key, inheritor);
                return inheritor;
            }
            else
            {
                return super.get(key);
            }
        }
    };
    private Engine engine;

    public Engine getEngine()
    {
        return engine;
    }

    public void setEngine(Engine engine)
    {
        this.engine = engine;
    }

    public void runProcedure(Procedure procedure, List<FormalElement> parameters) throws IFML2Exception
    {
        try
        {
            RunningContext runningContext = new RunningContext(parameters, this);
            loadGlobalVariables(runningContext);
            loadProcedureVariables(runningContext, procedure);
            runInstructionList(procedure.getProcedureBody(), runningContext, false, true);
            saveProcedureVariables(runningContext, procedure);
            saveGlobalVariables(runningContext);
        }
        catch (IFML2VMException e)
        {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    private void saveGlobalVariables(RunningContext runningContext)
    {
        for (Map.Entry<String, Value> var : engine.getGlobalVariables().entrySet())
        {
            Value value = runningContext.getVariable(Variable.VariableScope.GLOBAL, var.getKey());
            if (value != null)
            {
                engine.getGlobalVariables().put(var.getKey(), value);
            }
        }
    }

    private void loadGlobalVariables(RunningContext runningContext)
    {
        for (Map.Entry<String, Value> var : engine.getGlobalVariables().entrySet())
        {
            runningContext.setVariable(Variable.VariableScope.GLOBAL, var.getKey(), var.getValue());
        }
    }

    public void runAction(Action action, List<FormalElement> formalElements) throws IFML2Exception
    {
        runProcedure(action.getProcedureCall().getProcedure(), formalElements);
    }

    public void runProcedure(Procedure procedure) throws IFML2Exception
    {
        try
        {
            RunningContext runningContext = new RunningContext(this);
            loadGlobalVariables(runningContext);
            loadProcedureVariables(runningContext, procedure);
            runInstructionList(procedure.getProcedureBody(), runningContext, false, true);
            saveProcedureVariables(runningContext, procedure);
            saveGlobalVariables(runningContext);
        }
        catch (IFML2VMException e)
        {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    public void runHook(Hook hook, List<FormalElement> formalElements) throws IFML2Exception
    {
        RunningContext runningContext = new RunningContext(formalElements, this);
        loadGlobalVariables(runningContext);
        runInstructionList(hook.instructionList, runningContext, false, true);
        saveGlobalVariables(runningContext);
    }

    public void runInstructionList(InstructionList instructionList, RunningContext runningContext, boolean encloseContext, boolean returnResult) throws IFML2Exception
    {
        RunningContext instructionRunningContext;
        instructionRunningContext = encloseContext ? new RunningContext(runningContext) : runningContext;

        for (Instruction instruction : instructionList.getInstructions())
        {
            instruction.virtualMachine = this;
            try
            {
                instruction.run(instructionRunningContext);
            }
            catch (IFML2VMException e)
            {
                throw new IFML2VMException(e, "{0}\n  в инструкции #{1} ({2})", e.getMessage(),
                        instructionList.getInstructions().indexOf(instruction) + 1, instruction.toString());
            }
        }

        if (returnResult && runningContext != instructionRunningContext)
        {
            runningContext.setReturnValue(instructionRunningContext.getReturnValue());
        }
    }

    private void loadProcedureVariables(RunningContext runningContext, Procedure procedure) throws IFML2VMException
    {
        for (ProcedureVariable procedureVariable : procedure.getVariables())
        {
            try
            {
                Value value = procedureVariable.getValue();
                if (value == null) // first initialization of procedure variable
                {
                    value = ExpressionCalculator.calculate(runningContext, procedureVariable.getInitialValue());
                }
                runningContext.setVariable(Variable.VariableScope.PROCEDURE, procedureVariable.getName(), value);
            }
            catch (IFML2Exception e)
            {
                throw new IFML2VMException(e, "{0}\n  при инициализации переменной процедуры \"{1}\"", e.getMessage(),
                        procedureVariable.getName());
            }
        }
    }

    private void saveProcedureVariables(RunningContext runningContext, Procedure procedure)
    {
        for (ProcedureVariable procedureVariable : procedure.getVariables())
        {
            Value value = runningContext.getVariable(Variable.VariableScope.PROCEDURE, procedureVariable.getName());
            if (value != null)
            {
                procedureVariable.setValue(value);
            }
        }
    }

    public void showLocName(Location location) throws IFML2Exception
    {
        // test if inherited
        Procedure inheritor = inheritedSystemProcedures.get(SystemProcedureEnum.SHOW_LOC_NAME);

        if (inheritor != null)
        {
            // inherited! run inheritor
            runProcedure(inheritor);
        }
        else
        {
            // not inherited! do as usual...
            getEngine().outTextLn(location.getName());
            getEngine().outTextLn(location.getDescription());
            if (location.getItems().size() > 0)
            {
                String objectsList = convertObjectsToString(location.getItems());
                getEngine().outTextLn("А также тут " + objectsList);
            }
        }
    }

    public void showInventory()
    {
        if (engine.getInventory().size() > 0)
        {
            String objectsList = convertObjectsToString(engine.getInventory());
            engine.outTextLn("У Вас при себе " + objectsList);
        }
        else
        {
            engine.outTextLn("А у Вас ничего нет.");
        }
    }

    private String convertObjectsToString(List<Item> inventory)
    {
        String result = "";

        Iterator<Item> iterator = inventory.iterator();

        while (iterator.hasNext())
        {
            String itemName = iterator.next().getName();

            if ("".equals(result)) // it's the first word
            {
                result = itemName;
            }
            else if (iterator.hasNext()) // there is an another word after that
            {
                result += ", " + itemName;
            }
            else // it's the last word
            {
                result += " и " + itemName;
            }
        }

        result += ".";

        return result;
    }

    public Value resolveSymbol(String symbol) throws IFML2VMException
    {
        String loweredSymbol = symbol.toLowerCase();

        if (systemConstants.containsKey(loweredSymbol))
        {
            return systemConstants.get(loweredSymbol);
        }

        return engine.resolveSymbol(symbol);
    }

    public Value runTrigger(Trigger trigger, IFMLObject ifmlObject) throws IFML2Exception
    {
        RunningContext runningContext = new RunningContext(this);
        runningContext.setDefaultObject(ifmlObject);

        runInstructionList(trigger.getInstructions(), runningContext, false, true);

        return runningContext.getReturnValue();
    }
}
