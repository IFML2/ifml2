package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Property
{
    @XmlAttribute(name = PROPERTY_NAME_ATTRIBUE)
    private String name; //can't load as IDREF because this name isn't unique

    @XmlTransient
    private Role parentRole;

    /**
     * JAXB afterUnmarshal listener
     * @param unmarshaller Unmarshaller
     * @param parent Parent, should be Role
     */
    @SuppressWarnings("UnusedDeclaration")
    private void afterUnmarshal(final Unmarshaller unmarshaller,
                                final Object parent)
    {
        assert parent instanceof Role;
        parentRole = (Role) parent;
    }

    @XmlAttribute(name = PROPERTY_VALUE_ATTRIBUTE)
    private String value;

    @XmlElement(name = PROPERTY_COLLECTION_ITEM_ELEMENT)
    private EventList<String> collectionItems = new BasicEventList<String>();
}
