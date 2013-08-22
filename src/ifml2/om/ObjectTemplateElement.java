package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class ObjectTemplateElement extends TemplateElement
{
    private Word.GramCaseEnum gramCase = Word.GramCaseEnum.IP;

    @XmlAttribute(name = "case")
    public Word.GramCaseEnum getGramCase()
    {
        return gramCase;
    }

    public void setGramCase(Word.GramCaseEnum gramCase)
    {
        this.gramCase = gramCase;
    }

    @Override
    public String toString()
    {
        return getSimpleView() + (parameter != null ? " => " + parameter : "");
    }

    @Override
    public ObjectTemplateElement clone() throws CloneNotSupportedException
    {
        // basic shallow copy
        return (ObjectTemplateElement) super.clone();
    }

    @Override
    public String getSimpleView()
    {
        return gramCase.getAbbreviation();
    }
}
