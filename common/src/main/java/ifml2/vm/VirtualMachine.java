package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.IFMLObject;
import ifml2.om.InstructionList;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.Trigger;
import ifml2.vm.values.Value;

import java.util.List;

public interface VirtualMachine {

    void init();

    void setEngine(Engine engine);

    Value resolveSymbol(String symbol) throws IFML2VMException;

    Story getStory();

    Variable searchGlobalVariable(String name);

    void runProcedureWithoutParameters(Procedure procedure) throws IFML2Exception;

    void showLocation(Location location) throws IFML2Exception;

    void runHook(Hook hook, List<Variable> parameters) throws IFML2Exception;

    void runAction(Action action, List<Variable> parameters) throws IFML2Exception;

    Value callProcedureWithParameters(Procedure procedure, List<Variable> parameters) throws IFML2Exception;

    void runInstructionList(InstructionList instructionList, RunningContext runningContext) throws IFML2Exception;

    Value runTrigger(Trigger trigger, IFMLObject ifmlObject) throws IFML2Exception;

    void setCurrentLocation(Location location);

    void outText(String text);

    void outTextLn(String text, Object... args);

    void outPicture(String filePath, int maxHeight, int maxWidth);

}
