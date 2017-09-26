package ifml2.grammar.dictionary

import ifml2.grammar.word.Word

class NaiveSourceDictionary : SourceDictionary {

    private val dictionary: MutableMap<String, MutableList<Word>> = hashMapOf()

    private fun search(name: String): MutableList<Word> {
        return dictionary.getOrDefault(name, arrayListOf())
    }

    override fun find(word: String): List<Word> {
        return search(word).toList()
    }

    override fun add(word: Word) {
        val list = search(word.name)
        list.add(word)
        dictionary[word.name] = list
    }
}
