package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFMLEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

@XmlAccessorType(XmlAccessType.NONE)
public class Template extends IFMLEntity {
    @XmlElements({
            @XmlElement(name = "literalElement", type = LiteralTemplateElement.class),
            @XmlElement(name = "objectElement", type = ObjectTemplateElement.class)
    })
    private EventList<TemplateElement> elements = new BasicEventList<>();

    public EventList<TemplateElement> getElements() {
        return elements;
    }

    public void setElements(EventList<TemplateElement> elements) {
        this.elements = elements;
    }

    public int getSize() {
        return elements.size();
    }

    public TemplateElement get(int index) {
        return elements.get(index);
    }

    @Override
    public Template clone() throws CloneNotSupportedException {
        Template clone = (Template) super.clone(); // flat clone

        // deep clone
        clone.elements = deepCloneEventList(elements, TemplateElement.class);

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
}
