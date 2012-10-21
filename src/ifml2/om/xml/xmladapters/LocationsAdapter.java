package ifml2.om.xml.xmladapters;

import ifml2.om.Location;
import ifml2.om.xml.xmlobjects.XmlLocations;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationsAdapter extends XmlAdapter<XmlLocations, HashMap<String, Location>>
{

	@Override
	public XmlLocations marshal(HashMap<String, Location> v) throws Exception
	{
		XmlLocations xmlLocations = new XmlLocations();
        xmlLocations.locations = new ArrayList<Location>(v.values());
        return xmlLocations;
	}

	@Override
	public HashMap<String, Location> unmarshal(XmlLocations v)
			throws Exception
	{
		HashMap<String, Location> locations = new HashMap<String, Location>();
		for(Location location : v.locations)
		{
			locations.put(location.getId().toLowerCase(), location);
		}
		return locations;
	}


}
