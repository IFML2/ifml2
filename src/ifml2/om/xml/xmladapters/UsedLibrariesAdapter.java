package ifml2.om.xml.xmladapters;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFML2Exception;
import ifml2.om.Library;
import ifml2.om.OMManager;
import ifml2.om.xml.xmlobjects.XmlUsedLibrary;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;

public class UsedLibrariesAdapter extends XmlAdapter<XmlUsedLibrary, EventList<Library>>
{
	@Override
	public XmlUsedLibrary marshal(EventList<Library> v)
    {
		XmlUsedLibrary xmlUsedLibrary = new XmlUsedLibrary();
        ArrayList<String> usedLibrary = new ArrayList<String>();
        for(Library library : v)
        {
            usedLibrary.add(library.path);
        }
        xmlUsedLibrary.usedLibrary = usedLibrary;
        return xmlUsedLibrary;
	}

	@Override
	public EventList<Library> unmarshal(XmlUsedLibrary v) throws IFML2Exception
    {
        EventList<Library> libraries = new BasicEventList<Library>();
		for(String libPath : v.usedLibrary)
		{
			libraries.add(OMManager.loadLibrary(libPath));
		}
		
		return libraries;
	}
}
