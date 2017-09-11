package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import javax.xml.bind.annotation.XmlElement;

public class LiteralTemplateElement extends TemplateElement
{
    @XmlElement(name = "synonym")
    private EventList<String> synonyms = new BasicEventList<String>();
    public EventList<String> getSynonyms()
    {
        return synonyms;
    }

	@Override
	public String toString()
	{
		return (synonyms != null ? getSimpleView() : "") + (parameter != null ? " => " + parameter : "");
	}

    @Override
    public LiteralTemplateElement clone() throws CloneNotSupportedException
    {
        // basic shallow copy
        LiteralTemplateElement clone = (LiteralTemplateElement) super.clone();

        // copy synonyms
        clone.synonyms = GlazedLists.eventList(synonyms);

        return clone;
    }

    @Override
    public String getSimpleView()
    {
        String result = "";

        for (String synonym : synonyms)
        {
            if(result.length() > 0)
            {
                result += " | ";
            }

            result += "\"" + synonym + "\"";
        }

        return "{ " + result + " }";
    }
}
