package ifml2.grammar.dictionary

import ifml2.grammar.word.Unknown
import ifml2.grammar.word.Word

class EmptyDictionary : Dictionary {

    override fun find(word: String): List<Word> {
        return listOf(Unknown(word))
    }

    override fun add(word: Word) {
        // Nothing to do
    }

}
