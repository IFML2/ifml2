package ifml2.om;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class Template implements Cloneable {
    private EventList<TemplateElement> elements = new BasicEventList<TemplateElement>();

    @XmlElements({ @XmlElement(name = "literalElement", type = LiteralTemplateElement.class),
            @XmlElement(name = "objectElement", type = ObjectTemplateElement.class) })
    public EventList<TemplateElement> getElements() {
        return elements;
    }

    public int getSize() {
        return elements.size();
    }

    public TemplateElement get(int index) {
        return elements.get(index);
    }

    @Override
    public Template clone() throws CloneNotSupportedException {
        // basic shallow copy
        Template clone = (Template) super.clone();

        // clone and copy elements
        clone.elements = new BasicEventList<TemplateElement>();
        for (TemplateElement templateElement : elements) {
            clone.elements.add(templateElement.clone());
        }

        return clone;
    }

    @Override
    public String toString() {
        String result = "";
        for (TemplateElement element : elements) {
            if (result.length() > 0) {
                result += " + ";
            }

            result += element.getSimpleView();
        }

        return result;
    }

    public void setElements(EventList<TemplateElement> elements) {
        this.elements = elements;
    }
}
