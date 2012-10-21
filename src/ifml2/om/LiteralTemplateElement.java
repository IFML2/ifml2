package ifml2.om;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class LiteralTemplateElement extends TemplateElement
{
	//public String value;
    @XmlElement(name = "synonym")
    public List<String> synonyms;
	
	/*
    public LiteralTemplateElement(int position, List<String> synonyms)
	{
		this.position = position;
		//this.value = value;
        this.synonyms = synonyms;
	}
    */
    
	@Override
	public String toString()
	{
		return /*value + */(synonyms != null ? synonyms.toString() : "");
	}
}
