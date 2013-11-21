package ifml2.engine.savedGame;

import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("UnusedDeclaration")
public class ItemItems
{
    ItemItems()
    {

    }

    private String itemId = null;

    @XmlAttribute(name = "id")
    public String getItemId()
    {
        return itemId;
    }

    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }
}
