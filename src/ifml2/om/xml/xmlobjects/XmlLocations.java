package ifml2.om.xml.xmlobjects;

import ifml2.om.Location;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="locations")
public class XmlLocations
{
	@XmlElement(name="location")
	public List<Location> locations = new ArrayList<Location>();
}
