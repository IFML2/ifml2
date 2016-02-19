package ifml2.om;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ObjectTemplateElement extends TemplateElement {
    @XmlAttribute(name = "case")
    private Word.GramCase gramCase = Word.GramCase.IP;

    public Word.GramCase getGramCase() {
        return gramCase;
    }

    public void setGramCase(Word.GramCase gramCase) {
        this.gramCase = gramCase;
    }

    @Override
    public String toString() {
        return getSimpleView() + (parameter != null ? " => " + parameter : "");
    }

    @Override
    public ObjectTemplateElement clone() throws CloneNotSupportedException {
        return (ObjectTemplateElement) super.clone(); // just flat clone
    }

    @Override
    public String getSimpleView() {
        return gramCase.getAbbreviation();
    }
}
