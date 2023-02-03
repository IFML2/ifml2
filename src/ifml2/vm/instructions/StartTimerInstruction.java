package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.InstructionList;
import ifml2.om.xml.xmladapters.DurationXmlAdapter;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.Duration;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

@XmlRootElement(name = "startTimer")
@XmlAccessorType(NONE)
@IFML2Instruction(title = "⏰ Запустить таймер")
public class StartTimerInstruction extends Instruction {
    @XmlAttribute(name = "timerType")
    private Type timerType = Type.REAL_TIME; // default type

    @XmlAttribute(name = "duration")
    @XmlJavaTypeAdapter(DurationXmlAdapter.class)
    private Duration duration = Duration.ZERO;

    @XmlAttribute(name = "actionCount")
    private int actionCount;

    @XmlElement(name = "reaction")
    private InstructionList instructions = new InstructionList();

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception {
        virtualMachine.startRealTimeTimer(duration, instructions, runningContext);
    }

    public Type getTimerType() {
        return timerType;
    }

    public void setTimerType(Type timerType) {
        this.timerType = timerType;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int getActionCount() {
        return actionCount;
    }

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public InstructionList getInstructions() {
        return instructions;
    }

    public void setInstructions(InstructionList instructionList) {
        this.instructions = instructionList;
    }

    @XmlEnum
    @XmlType(namespace = "StartTimerInstruction")
    public enum Type {
        @XmlEnumValue(value = "realTime")
        REAL_TIME,
        @XmlEnumValue(value = "actionsCount")
        ACTION_COUNT
    }

    @Override
    public String toString() {
        final String name = "⏰ Запустить таймер";
        switch (timerType) {
            case REAL_TIME:
                return String.format("%s по реальному времени на %d секунд", name, duration.getSeconds());
            case ACTION_COUNT:
                return String.format("%s через %d ходов", name, actionCount);
            default:
                throw new IllegalStateException("Unexpected value: " + timerType);
        }
    }
}
