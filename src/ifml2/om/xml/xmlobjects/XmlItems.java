package ifml2.om.xml.xmlobjects;

import ifml2.om.Item;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="items")
public class XmlItems
{
	@XmlElement(name="item")
	public List<Item> items = new ArrayList<Item>();
}
