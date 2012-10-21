package ifml2.om.xml.xmladapters;

import ifml2.om.Word;
import ifml2.om.xml.xmlobjects.XmlDictionary;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class DictionaryAdapter extends XmlAdapter<XmlDictionary, HashMap<String, Word>>
{
	@Override
	public HashMap<String, Word> unmarshal(XmlDictionary v) throws Exception
	{
		HashMap<String, Word> dictionary = new HashMap<String, Word>();
		for(Word word : v.words)
		{
			dictionary.put(word.ip.toLowerCase(), word);
		}
		
		return dictionary;
	}

	@Override
	public XmlDictionary marshal(HashMap<String, Word> v) throws Exception
	{
		XmlDictionary xmlDictionary = new XmlDictionary();
        xmlDictionary.words = new ArrayList<Word>(v.values());
        return xmlDictionary;
	}

}
