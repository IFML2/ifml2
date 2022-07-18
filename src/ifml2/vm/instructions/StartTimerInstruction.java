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
@IFML2Instruction(title = "Запустить таймер")
public class StartTimerInstruction extends Instruction {
    @XmlAttribute(name = "timerType")
    Type timerType;

    @XmlAttribute(name = "duration")
    @XmlJavaTypeAdapter(DurationXmlAdapter.class)
    Duration duration;

    @XmlAttribute(name = "actionCount")
    int actionCount;

    @XmlElement(name = "reaction")
    private final InstructionList instructions = new InstructionList();

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception {
        virtualMachine.startRealTimeTimer(duration, instructions, runningContext);
    }

    @XmlEnum
    @XmlType(namespace = "StartTimerInstruction")
    public enum Type {
        @XmlEnumValue(value = "realTime")
        REAL_TIME,
        @XmlEnumValue(value = "actionsCount")
        ACTIONS_COUNT
    }

    @Override
    public String toString() {
        final String name = "Запустить таймер ";
        switch (timerType) {
            case REAL_TIME:
                return String.format("%s по реальному времени на %s", name, duration);
            case ACTIONS_COUNT:
                return String.format("%s через %d ходов", name, actionCount);
            default:
                throw new IllegalStateException("Unexpected value: " + timerType);
        }
    }
}
