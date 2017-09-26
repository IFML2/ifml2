package ifml2.grammar.source

import ifml2.grammar.word.*
import ifml2.grammar.word.parts.Case
import ifml2.grammar.word.parts.Gender
import ifml2.grammar.word.parts.Quantity

interface NameSource : WordSource {

    override val name: String
    val cases: Map<Case, String>
    val gender: Gender
    val quantity: Quantity
    val animacy: Boolean

    fun asCase(case: Case): Name

    override fun toDictionary(): List<Word> {
        return Case.values().toList().map { asCase(it) }
    }

}
