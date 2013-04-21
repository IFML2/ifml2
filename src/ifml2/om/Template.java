package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class Template implements Cloneable
{
    private EventList<TemplateElement> elements = new BasicEventList<TemplateElement>();
    @XmlElements({
            @XmlElement(name = "literalElement", type = LiteralTemplateElement.class),
            @XmlElement(name = "objectElement", type = ObjectTemplateElement.class)
    })
    public EventList<TemplateElement> getElements()
    {
        return elements;
    }

    public int getSize()
    {
        return elements.size();
    }

    public TemplateElement get(int index)
    {
        return elements.get(index);
    }

    @Override
    public Template clone() throws CloneNotSupportedException
    {
        // basic shallow copy
        Template clone = (Template) super.clone();

        // clone and copy elements
        clone.elements = new BasicEventList<TemplateElement>();
        for(TemplateElement templateElement : elements)
        {
            clone.elements.add(templateElement.clone());
        }

        return clone;
    }

    @Override
    public String toString()
    {
        return elements.toString();
    }

    public void setElements(EventList<TemplateElement> elements)
    {
        this.elements = elements;
    }
}
