package ifml2.grammar.dictionary

import ifml2.grammar.word.Word

class CompositeDictionary(
        val dictionaries: List<Dictionary>
) : Dictionary {

    override fun find(word: String): List<Word> {
        return dictionaries.flatMap { it.find(word) }
    }

    override fun add(word: Word) {
        // Nothing to do
    }
}
