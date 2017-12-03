package ifml2.om.xml.xmladapters;

import java.util.ArrayList;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFML2Exception;
import ifml2.om.Library;
import ifml2.om.OMManager;
import ifml2.om.xml.xmlobjects.XmlUsedLibrary;

public class UsedLibrariesAdapter extends XmlAdapter<XmlUsedLibrary, EventList<Library>> {
    private final static Logger LOG = LoggerFactory.getLogger(UsedLibrariesAdapter.class);

    @Override
    public XmlUsedLibrary marshal(EventList<Library> libraries) {
        XmlUsedLibrary xmlUsedLibrary = new XmlUsedLibrary();
        ArrayList<String> usedLibrary = new ArrayList<String>();
        for (Library library : libraries) {
            usedLibrary.add(library.getPath());
        }
        xmlUsedLibrary.usedLibrary = usedLibrary;
        return xmlUsedLibrary;
    }

    @Override
    public EventList<Library> unmarshal(XmlUsedLibrary xmlUsedLibrary) throws IFML2Exception {
        LOG.trace(String.format("unmarshal(XmlUsedLibrary = %s)", xmlUsedLibrary));

        EventList<Library> libraries = new BasicEventList<Library>();
        for (String libPath : xmlUsedLibrary.usedLibrary) {
            libraries.add(OMManager.loadLibrary(libPath));
        }

        LOG.trace("unmarshal() :: END");
        return libraries;
    }
}
