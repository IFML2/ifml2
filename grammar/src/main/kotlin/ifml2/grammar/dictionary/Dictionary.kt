package ifml2.grammar.dictionary

import ifml2.grammar.word.Word

interface Dictionary {
    fun find(word: String): List<Word>
    fun add(word: Word)
}
