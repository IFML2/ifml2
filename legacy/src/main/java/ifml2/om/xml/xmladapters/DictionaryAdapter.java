package ifml2.om.xml.xmladapters;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ifml2.om.Word;
import ifml2.om.xml.xmlobjects.XmlDictionary;

public class DictionaryAdapter extends XmlAdapter<XmlDictionary, HashMap<String, Word>> {
    @Override
    public HashMap<String, Word> unmarshal(XmlDictionary xmlDictionary) throws Exception {
        HashMap<String, Word> dictionary = new HashMap<String, Word>();
        for (Word word : xmlDictionary.words) {
            dictionary.put(word.ip.toLowerCase(), word);
        }

        return dictionary;
    }

    @Override
    public XmlDictionary marshal(HashMap<String, Word> wordHashMap) throws Exception {
        XmlDictionary xmlDictionary = new XmlDictionary();
        xmlDictionary.words = new ArrayList<Word>(wordHashMap.values());
        return xmlDictionary;
    }
}
