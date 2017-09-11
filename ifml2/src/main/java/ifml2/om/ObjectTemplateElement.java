package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class ObjectTemplateElement extends TemplateElement {
    private Word.GramCase gramCase = Word.GramCase.IP;

    @XmlAttribute(name = "case")
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
        // basic shallow copy
        return (ObjectTemplateElement) super.clone();
    }

    @Override
    public String getSimpleView() {
        return gramCase.getAbbreviation();
    }
}
