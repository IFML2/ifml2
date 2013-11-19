package ifml2.engine.savedGame;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class LocItems
{
    public LocItems()
    {

    }

    private String locId = null;

    private List<String> items = new ArrayList<String>();

    public LocItems(String locId)
    {
        this();
        this.locId = locId;
    }

    public List<String> getItems()
    {
        return items;
    }

    public String getLocId()
    {
        return locId;
    }

    @XmlAttribute(name = "id")
    public void setLocId(String locId)
    {
        this.locId = locId;
    }

    @XmlElement(name = "item")
    public void setItems(List<String> items)
    {
        this.items = items;
    }

    public void addItemId(String itemId)
    {
        items.add(itemId);
    }
}
