package ifml2.om;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class LiteralTemplateElement extends TemplateElement
{
    @XmlElement(name = "synonym")
    public List<String> synonyms;

	@Override
	public String toString()
	{
		return (synonyms != null ? synonyms.toString() : "");
	}
}
