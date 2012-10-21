package ifml2.om.xml.xmladapters;

import ifml2.om.Item;
import ifml2.om.xml.xmlobjects.XmlItems;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemsAdapter extends XmlAdapter<XmlItems, HashMap<String, Item>>
{
	@Override
	public XmlItems marshal(HashMap<String, Item> v) throws Exception
	{
		XmlItems xmlItems = new XmlItems();
        xmlItems.items = new ArrayList<Item>(v.values());
        return xmlItems;
	}

	@Override
	public HashMap<String, Item> unmarshal(XmlItems v) throws Exception
	{
		HashMap<String, Item> items = new HashMap<String, Item>();
		for(Item item : v.items)
		{
			items.put(item.getId().toLowerCase(), item);
		}
		return items;
	}
}
