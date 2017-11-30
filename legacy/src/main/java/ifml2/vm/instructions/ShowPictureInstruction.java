package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static javax.xml.bind.annotation.XmlAccessType.NONE;

@XmlRootElement(name = "showPicture")
@XmlAccessorType(NONE)
@IFML2Instruction(title = "Вывести картинку")
public class ShowPictureInstruction extends Instruction {
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

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    @Override
    public String toString() {
        String restrictions = "";
        if (maxHeight > 0 || maxWidth > 0) {
            restrictions += " с ограничениями";
            if (maxHeight > 0) {
                restrictions += format(" по высоте %d", maxHeight);
            }
            if (maxWidth > 0) {
                restrictions += format(" по ширине %d", maxWidth);
            }
        }
        return format("Вывести картинку '%s'%s", getFileName(), restrictions);
    }

    private String getFileName() {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }
}
