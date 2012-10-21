package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Property
{
    @XmlAttribute(name = PROPERTY_NAME_ATTRIBUE)
    private String name;

    @XmlAttribute(name = PROPERTY_VALUE_ATTRIBUTE)
    private String value;

    @XmlElement(name = PROPERTY_COLLECTION_ITEM_ELEMENT)
    private EventList<String> collectionItems = new BasicEventList<String>();
}
