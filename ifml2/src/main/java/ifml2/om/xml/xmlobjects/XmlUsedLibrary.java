package ifml2.om.xml.xmlobjects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "libraries")
public class XmlUsedLibrary {
    @XmlElement(name = "library")
    public List<String> usedLibrary = new ArrayList<String>();

    @Override
    public String toString() {
        return String.format("{usedLibrary = %s}", usedLibrary.toString());
    }
}
