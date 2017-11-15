package ifml2.vm;

import static ifml2.om.Procedure.SystemProcedureType.SHOW_LOCATION;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ifml2.Environment;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.IFMLObject;
import ifml2.om.InstructionList;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Procedure.SystemProcedureType;
import ifml2.om.Story;
import ifml2.om.Trigger;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.EmptyValue;
import ifml2.vm.values.Value;

public class VirtualMachineImpl implements VirtualMachine {

    public VirtualMachineImpl(final Environment environment) {
        this.environment = environment;
    }

    private final Map<String, Value> systemConstants = new HashMap<String, Value>() {
        {
            put(BooleanValue.TRUE, new BooleanValue(true));
            put(BooleanValue.FALSE, new BooleanValue(false));
            put(EmptyValue.EMPTY, new EmptyValue());
        }
    };

    private Engine engine;
    private Environment environment;

    private final Map<SystemProcedureType, Procedure> inheritedSystemProcedures = new HashMap<SystemProcedureType, Procedure>() {
        @Override
        public Procedure get(Object key) {
            // lazy initialization
            if (!containsKey(key)) {
                Procedure inheritor = engine.getStory().getSystemInheritorProcedure((SystemProcedureType) key);
                put((SystemProcedureType) key, inheritor);
                return inheritor;
            } else {
                return super.get(key);
            }
        }
    };

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void runAction(Action action, List<Variable> parameters) throws IFML2Exception {
        runProcedure(action.getProcedureCall().getProcedure(), parameters);
    }

    public void runProcedureWithoutParameters(Procedure procedure) throws IFML2Exception {
        try {
            RunningContext runningContext = createCallContext(procedure, null);
            runInstructionList(procedure.getProcedureBody(), runningContext);
        } catch (IFML2VMException e) {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    public Value callProcedureWithParameters(Procedure procedure, List<Variable> parameters) throws IFML2Exception {
        try {
            RunningContext runningContext = createCallContext(procedure, parameters);
            runInstructionList(procedure.getProcedureBody(), runningContext);
            return runningContext.getReturnValue();
        } catch (IFML2VMException e) {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    void runProcedure(Procedure procedure, List<Variable> parameters) throws IFML2Exception {
        try {
            RunningContext runningContext = createCallContext(procedure, parameters);
            runInstructionList(procedure.getProcedureBody(), runningContext);
        } catch (IFML2VMException e) {
            throw new IFML2VMException(e, "{0}\n  в процедуре \"{1}\"", e.getMessage(), procedure.getName());
        }
    }

    public void runHook(Hook hook, List<Variable> parameters) throws IFML2Exception {
        RunningContext runningContext = createRunningContext();
        runningContext.populateParameters(parameters);
        runInstructionList(hook.getInstructionList(), runningContext);
    }

    public void runInstructionList(InstructionList instructionList, RunningContext runningContext) throws IFML2Exception {
        for (Instruction instruction : instructionList.getInstructions()) {
            instruction.virtualMachine = this;
            try {
                instruction.run(runningContext);
            } catch (IFML2VMException e) {
                throw new IFML2VMException(e, "{0}\n  в инструкции #{1} ({2})", e.getMessage(),
                        instructionList.getInstructions().indexOf(instruction) + 1, instruction.toString());
            }
        }
    }

    public void showLocation(Location location) throws IFML2Exception {
        if (location == null) {
            return;
        }

        // check if inherited
        Procedure inheritor = inheritedSystemProcedures.get(SHOW_LOCATION);

        if (inheritor != null) {
            // inherited! run inheritor
            runProcedureWithoutParameters(inheritor);
        } else {
            // not inherited! do as usual...
            outTextLn(location.getName());
            outTextLn(location.getDescription());
            if (!location.getItems().isEmpty()) {
                outTextLn("А также тут {0}", convertObjectsToString(location.getItems()));
            }
        }
    }

    private String convertObjectsToString(List<Item> inventory) {
        StringBuilder sb = new StringBuilder();

        Iterator<Item> iterator = inventory.iterator();

        if (iterator.hasNext()) {
            sb.append(iterator.next().getName());
        }

        while (iterator.hasNext()) {
            sb.append(iterator.hasNext() ? ", " : " и ")
                .append(iterator.hasNext());
            String itemName = iterator.next().getName();
        }
        return sb.toString();
    }

    public Value resolveSymbol(String symbol) throws IFML2VMException {
        String loweredSymbol = symbol.toLowerCase();

        if (systemConstants.containsKey(loweredSymbol)) {
            return systemConstants.get(loweredSymbol);
        }

        return engine.resolveSymbol(symbol);
    }

    public Value runTrigger(Trigger trigger, IFMLObject ifmlObject) throws IFML2Exception {
        RunningContext runningContext = createRunningContext();
        runningContext.setDefaultObject(ifmlObject);

        runInstructionList(trigger.getInstructions(), runningContext);

        return runningContext.getReturnValue();
    }

    public void setCurrentLocation(Location location) {
        engine.setCurrentLocation(location);
    }

    public Story getStory() {
        return environment.getStory();
    }

    public void outTextLn(String text, Object... args) {
        environment.outText(text + "\n", args);
    }

    public void outText(String text) {
        environment.outText(text);
    }

    public Variable searchGlobalVariable(String name) {
        return engine.searchGlobalVariable(name);
    }

    public void init() {
        inheritedSystemProcedures.clear();
    }

    public void outPicture(String filePath, int maxHeight, int maxWidth) {
        environment.outIcon(filePath, maxHeight, maxWidth);
    }

    @Override
    public RunningContext createRunningContext() {
        return new RunningContext(this);
    }

    @Override
    public RunningContext createCallContext(final Procedure contextProcedure, final List<Variable> paramters) {
        final RunningContext runningContext = new RunningContext(this, contextProcedure);

        if (paramters != null && !paramters.isEmpty()) {
            paramters.stream()
                    .filter(parameter -> parameter.name != null)
                    .forEach(runningContext::putVariable);
        }

        contextProcedure.getParameters().stream()
                .map(Parameter::getName)
                .filter(runningContext::isLocalVariableNotExists)
                .forEach(runningContext::writeEmptyLocalVariable);

        return runningContext;
    }

    @Override
    public RunningContext createNestedContext(final RunningContext parentRunningContext) {
        final RunningContext runningContext = new RunningContext(this);

        parentRunningContext.getVariables().forEach(runningContext::writeLocalVariable);

        return runningContext;
    }

}
