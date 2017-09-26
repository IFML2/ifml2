package ifml2.grammar.source

import ifml2.grammar.word.Word

interface WordSource {

    val name: String // Initial form

    fun toDictionary() : List<Word>

}
