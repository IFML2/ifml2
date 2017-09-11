package ifml2.om.xml.xmlobjects;

import ifml2.om.Word;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "dictionary")
public class XmlDictionary {
    @XmlElement(name = "word")
    public List<Word> words = new ArrayList<Word>();
}
