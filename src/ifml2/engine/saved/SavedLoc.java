package ifml2.engine.saved;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class SavedLoc
{
    @XmlAttribute(name = "id")
    private String locId = null;
    private List<String> items = new ArrayList<String>();

    @SuppressWarnings("UnusedDeclaration")
    public SavedLoc()
    {
        // for JAXB
    }

    public SavedLoc(String locId)
    {
        this.locId = locId;
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

    public String getLocId()
    {
        return locId;
    }

    public void addItemId(String itemId)
    {
        items.add(itemId);
    }
}
