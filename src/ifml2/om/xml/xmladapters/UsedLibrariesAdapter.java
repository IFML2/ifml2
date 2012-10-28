package ifml2.om.xml.xmladapters;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFML2Exception;
import ifml2.om.Library;
import ifml2.om.OMManager;
import ifml2.om.xml.xmlobjects.XmlUsedLibrary;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;

public class UsedLibrariesAdapter extends XmlAdapter<XmlUsedLibrary, EventList<Library>>
{
	private final static Logger LOG = Logger.getLogger(UsedLibrariesAdapter.class);

    @Override
	public XmlUsedLibrary marshal(EventList<Library> v)
    {
		XmlUsedLibrary xmlUsedLibrary = new XmlUsedLibrary();
        ArrayList<String> usedLibrary = new ArrayList<String>();
        for(Library library : v)
        {
            usedLibrary.add(library.getPath());
        }
        xmlUsedLibrary.usedLibrary = usedLibrary;
        return xmlUsedLibrary;
	}

	@Override
	public EventList<Library> unmarshal(XmlUsedLibrary v) throws IFML2Exception
    {
        LOG.trace(String.format("unmarshal(XmlUsedLibrary = %s)", v));

        EventList<Library> libraries = new BasicEventList<Library>();
		for(String libPath : v.usedLibrary)
		{
			libraries.add(OMManager.loadLibrary(libPath));
		}
		
		LOG.trace("unmarshal() :: END");
        return libraries;
	}
}
