package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Role
{
    @XmlAttribute(name = ROLE_NAME_ATTRIBUTE)
    private String name;

    @XmlElementWrapper(name = ROLE_PROPERTIES_ELEMENT)
    @XmlElement(name = ROLE_PROPERTY_ELEMENT)
    private EventList<Property> properties = new BasicEventList<Property>();
}
