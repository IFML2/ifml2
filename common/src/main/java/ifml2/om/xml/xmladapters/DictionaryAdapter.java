package ifml2.om.xml.xmladapters;

import ifml2.om.Word;
import ifml2.om.xml.xmlobjects.XmlDictionary;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DictionaryAdapter extends XmlAdapter<XmlDictionary, Map<String, Word>> {
    @Override
    public Map<String, Word> unmarshal(XmlDictionary xmlDictionary) throws Exception {
        Map<String, Word> dictionary = new HashMap<>();
        xmlDictionary.words.forEach(word -> { dictionary.put(word.ip.toLowerCase(), word); });
        return dictionary;
    }

    @Override
    public XmlDictionary marshal(Map<String, Word> wordHashMap) throws Exception {
        XmlDictionary xmlDictionary = new XmlDictionary();
        xmlDictionary.words = new ArrayList<>(wordHashMap.values());
        return xmlDictionary;
    }
}
