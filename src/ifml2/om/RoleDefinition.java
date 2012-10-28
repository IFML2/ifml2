package ifml2.om;

import ca.odell.glazedlists.BasicEventList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

class RoleDefinition
{
    @XmlAttribute(name = "name")
    public String name;

    @XmlAttribute(name = "description")
    public String description;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<PropertyDefinition> propertyDefinitions = new BasicEventList<PropertyDefinition>();
}
