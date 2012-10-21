package ifml2.om.xml.xmladapters;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.om.Attribute;
import ifml2.om.xml.xmlobjects.XmlAttributes;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AttributesAdapter extends XmlAdapter<XmlAttributes, EventList<Attribute>>
{
    @Override
    public EventList<Attribute> unmarshal(XmlAttributes v) throws Exception
    {
        // pre load just reference names ... to post load by OMManager later

        EventList<Attribute> attributes = new BasicEventList<Attribute>();

        for(String attributeRef : v.attributeRefs)
        {
            Attribute attribute = new Attribute(attributeRef);
            attributes.add(attribute);
        }

        return attributes;
    }

    @Override
    public XmlAttributes marshal(EventList<Attribute> v) throws Exception
    {
        // marshal only names as references

        XmlAttributes xmlAttributes = new XmlAttributes();

        for(Attribute attribute : v)
        {
            xmlAttributes.attributeRefs.add(attribute.getName());
        }

        return xmlAttributes;
    }
}
