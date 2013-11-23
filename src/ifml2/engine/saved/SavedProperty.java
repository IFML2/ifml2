package ifml2.engine.saved;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class SavedProperty
{
    @SuppressWarnings("UnusedDeclaration")
    public SavedProperty()
    {
    }

    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "item")
    private ArrayList<String> items = new ArrayList<String>();

    public SavedProperty(String propertyName)
    {
        name = propertyName;
    }

    public void addItem(String itemId)
    {
        getItems().add(itemId);
    }

    public String getName()
    {
        return name;
    }

    public ArrayList<String> getItems()
    {
        return items;
    }
}
