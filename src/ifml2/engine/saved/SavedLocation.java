package ifml2.engine.saved;

import ifml2.FormatLogger;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Story;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class SavedLocation
{
    private static final FormatLogger LOG = FormatLogger.getLogger(SavedLocation.class);
    @XmlAttribute(name = "id")
    private String id;

    private List<String> items = new ArrayList<String>();

    @SuppressWarnings("UnusedDeclaration")
    public SavedLocation()
    {
        // for JAXB
    }

    public SavedLocation(Location location)
    {
        id = location.getId();
        for (Item item : location.getItems())
        {
            items.add(item.getId());
        }
    }

    public List<String> getItems()
    {
        return items;
    }

    @XmlElement(name = "item")
    public void setItems(List<String> items)
    {
        this.items = items;
    }

    public String getId()
    {
        return id;
    }

    public void restore(Story.DataHelper dataHelper)
    {
        Location location = dataHelper.findLocationById(id);
        if (location != null)
        {
            List<Item> locationItems = location.getItems();
            locationItems.clear();
            for (String itemId : items)
            {
                Item item = dataHelper.findItemById(itemId);
                if (item != null)
                {
                    locationItems.add(item);
                    item.setContainer(locationItems); // todo refactor to set in OM in one action
                }
                else
                {
                    LOG.warn("[Game loading] Location items loading: there is no item with id \"{0}\".", itemId);
                }
            }
        }
        else
        {
            LOG.warn("[Game loading] Location items loading: there is no location with id \"{0}\".", id);
        }
    }
}
