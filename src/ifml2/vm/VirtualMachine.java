package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.SystemIdentifiers;
import ifml2.engine.Engine;
import ifml2.om.*;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.EmptyValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static ifml2.om.Procedure.SystemProcedureEnum;

public class VirtualMachine
{
    private final HashMap<String, Value> systemConstants = new HashMap<String, Value>()
    {
        {
            put(SystemIdentifiers.TRUE_BOOL_LITERAL, new BooleanValue(true));
            put(SystemIdentifiers.FALSE_BOOL_LITERAL, new BooleanValue(false));
            put(SystemIdentifiers.EMPTY_VALUE, new EmptyValue());
        }
    };
    private Engine engine;
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

    public Engine getEngine()
    {
        return engine;
    }

    public void setEngine(Engine engine)
    {
        this.engine = engine;
    }

    public void runAction(@NotNull Action action, List<Variable> parameters) throws IFML2Exception
    {
        runProcedure(action.getProcedureCall().getProcedure(), parameters);
    }

    public void runProcedureWithoutParameters(@NotNull Procedure procedure) throws IFML2Exception
    {
        try
        {
            RunningContext runningContext = RunningContext.CreateCallContext(this, procedure, null);
            runInstructionList(procedure.getProcedureBody(), runningContext);
        }
        catch (IFML2VMException e)
        {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    public Value callProcedureWithParameters(@NotNull Procedure procedure, List<Variable> parameters) throws IFML2Exception
    {
        try
        {
            RunningContext runningContext = RunningContext.CreateCallContext(this, procedure, parameters);
            runInstructionList(procedure.getProcedureBody(), runningContext);
            return runningContext.getReturnValue();
        }
        catch (IFML2VMException e)
        {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    void runProcedure(@NotNull Procedure procedure, List<Variable> parameters) throws IFML2Exception
    {
        try
        {
            RunningContext runningContext = RunningContext.CreateCallContext(this, procedure, parameters);
            runInstructionList(procedure.getProcedureBody(), runningContext);
        }
        catch (IFML2VMException e)
        {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    public void runHook(@NotNull Hook hook, List<Variable> parameters) throws IFML2Exception
    {
        RunningContext runningContext = RunningContext.CreateNewContext(this);
        runningContext.populateParameters(parameters);
        runInstructionList(hook.getInstructionList(), runningContext);
    }

    public void runInstructionList(@NotNull InstructionList instructionList, @NotNull RunningContext runningContext) throws IFML2Exception
    {
        for (Instruction instruction : instructionList.getInstructions())
        {
            instruction.virtualMachine = this;
            try
            {
                instruction.run(runningContext);
            }
            catch (IFML2VMException e)
            {
                throw new IFML2VMException(e, "{0}\n  в инструкции #{1} ({2})", e.getMessage(),
                        instructionList.getInstructions().indexOf(instruction) + 1, instruction.toString());
            }
        }
    }

    public void showLocation(@Nullable Location location) throws IFML2Exception
    {
        if(location == null)
        {
            return;
        }

        // check if inherited
        Procedure inheritor = inheritedSystemProcedures.get(SystemProcedureEnum.SHOW_LOCATION);

        if (inheritor != null)
        {
            // inherited! run inheritor
            runProcedureWithoutParameters(inheritor);
        }
        else
        {
            // not inherited! do as usual...
            engine.outTextLn(location.getName());
            engine.outTextLn(location.getDescription());
            if (location.getItems().size() > 0)
            {
                String objectsList = convertObjectsToString(location.getItems());
                engine.outTextLn("А также тут " + objectsList);
            }
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
        RunningContext runningContext = RunningContext.CreateNewContext(this);
        runningContext.setDefaultObject(ifmlObject);

        runInstructionList(trigger.getInstructions(), runningContext);

        return runningContext.getReturnValue();
    }

    public void setCurrentLocation(Location location)
    {
        engine.setCurrentLocation(location);
    }

    public Story getStory()
    {
        return engine.getStory();
    }

    public void outTextLn(String text)
    {
        engine.outTextLn(text);
    }

    public void outText(String text)
    {
        engine.outText(text);
    }

    public Variable searchGlobalVariable(String name)
    {
        return engine.searchGlobalVariable(name);
    }

    /**
     * Initializes (resets) virtual machine.
     */
    public void init()
    {
        inheritedSystemProcedures.clear();
    }
}
