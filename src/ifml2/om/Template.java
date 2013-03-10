package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class Template
{
    private final EventList<TemplateElement> elements = new BasicEventList<TemplateElement>();
    @XmlElements({
            @XmlElement(name = "literalElement", type = LiteralTemplateElement.class),
            @XmlElement(name = "objectElement", type = ObjectTemplateElement.class)
    })
    public EventList<TemplateElement> getElements()
    {
        return elements;
    }

    public int size()
    {
        return elements.size();
    }

    public TemplateElement get(int index)
    {
        return elements.get(index);
    }
}
