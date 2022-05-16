package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import static java.lang.String.format;
import static javax.xml.bind.annotation.XmlAccessType.NONE;

@XmlRootElement(name = "playMusic")
@XmlAccessorType(NONE)
@IFML2Instruction(title = "Проиграть музыку")
public class PlayMusicInstruction extends Instruction {
    @XmlAttribute(name = "name")
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception {
        virtualMachine.playMusic(name);
    }

    @Override
    public String toString() {
        return format("Включить музыку \"%s\"", name);
    }
}
