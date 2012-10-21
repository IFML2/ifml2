package ifml2.om.xml.xmlobjects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.OBJECT_ATTRIBUTES_TAG;
import static ifml2.om.xml.XmlSchemaConstants.OBJECT_ATTRIBUTE_TAG;

@XmlRootElement(name = OBJECT_ATTRIBUTES_TAG)
public class XmlAttributes
{
    @XmlElement(name = OBJECT_ATTRIBUTE_TAG)
    public final List<String> attributeRefs = new ArrayList<String>();
}
