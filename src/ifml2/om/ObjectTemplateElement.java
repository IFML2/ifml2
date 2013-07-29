package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class ObjectTemplateElement extends TemplateElement
{
    @XmlAttribute(name = "case")
    private Word.GramCaseEnum gramCase = Word.GramCaseEnum.IP;
    public Word.GramCaseEnum getGramCase()
    {
        return gramCase;
    }

	@Override
	public String toString()
	{
		return gramCase.getAbbreviation() + (parameter != null ? " => " + parameter : "");
	}

    @Override
    public ObjectTemplateElement clone() throws CloneNotSupportedException
    {
        // basic shallow copy
        return (ObjectTemplateElement) super.clone();
    }
}
