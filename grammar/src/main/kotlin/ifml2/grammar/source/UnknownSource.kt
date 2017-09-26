package ifml2.grammar.source

import ifml2.grammar.word.Unknown
import ifml2.grammar.word.Word

class UnknownSource(override val name: String) : WordSource {
    override fun toDictionary(): List<Word> {
        return listOf(Unknown(name))
    }
}
