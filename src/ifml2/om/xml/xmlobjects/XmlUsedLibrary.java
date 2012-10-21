package ifml2.om.xml.xmlobjects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="libraries")
public class XmlUsedLibrary 
{
	@XmlElement(name="library")
	public List<String> usedLibrary;
}
