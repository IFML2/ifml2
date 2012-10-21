package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;

public class ObjectTemplateElement extends TemplateElement
{
    @XmlAttribute(name = "case")
    public Word.GramCaseEnum gramCase;

	@Override
	public String toString()
	{
		return gramCase.toString();
	}
}
