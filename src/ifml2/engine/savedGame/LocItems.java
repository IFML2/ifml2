package ifml2.engine.savedGame;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class LocItems
{
    public LocItems()
    {

    }

    @XmlAttribute(name = "id")
    public String locId = null;

    //@XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<String> items = new ArrayList<String>();

    public LocItems(String locId)
    {
        this();
        this.locId = locId;
    }

    public List<String> getItems()
    {
        return items;
    }
}
