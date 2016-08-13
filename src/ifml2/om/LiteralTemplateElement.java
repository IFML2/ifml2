package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class LiteralTemplateElement extends TemplateElement {
    @XmlElement(name = "synonym")
    private EventList<String> synonyms = new BasicEventList<>();

    public EventList<String> getSynonyms() {
        return synonyms;
    }

    @Override
    public String toString() {
        return (synonyms != null ? toSimpleView() : "") + (parameter != null ? " => " + parameter : "");
    }

    @Override
    public LiteralTemplateElement clone() throws CloneNotSupportedException {
        LiteralTemplateElement clone = (LiteralTemplateElement) super.clone(); // flat clone

        // deep clone
        clone.synonyms = GlazedLists.eventList(synonyms); // can't deepClone cause String isn't IFMLEntity

        return clone;
    }

    @Override
    public String toSimpleView() {
        String result = "";

        for (String synonym : synonyms) {
            if (result.length() > 0) {
                result += " | ";
            }

            result += "\"" + synonym + "\"";
        }

        return "{ " + result + " }";
    }

    public void copyTo(LiteralTemplateElement element) {
        super.copyTo(element);
        element.synonyms = GlazedLists.eventList(synonyms);
    }
}
