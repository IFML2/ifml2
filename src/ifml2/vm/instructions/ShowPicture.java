package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

@XmlRootElement(name = "showPicture")
@XmlAccessorType(NONE)
@IFML2Instruction(title = "Вывести картинку")
public class ShowPicture extends Instruction {
    @XmlAttribute(name = "filepath")
    private String filePath;
    @XmlAttribute(name = "maxHeight")
    private int maxHeight;
    @XmlAttribute(name = "maxWidth")
    private int maxWidth;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception {
        virtualMachine.outPicture(filePath, maxHeight, maxWidth);
    }
}
