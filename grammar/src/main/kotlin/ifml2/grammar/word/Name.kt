package ifml2.grammar.word

import ifml2.grammar.source.NameSource
import ifml2.grammar.word.parts.Case
import ifml2.grammar.word.parts.Gender
import ifml2.grammar.word.parts.Quantity

interface Name: Word {
    override val name: String
    val gender: Gender
    val case: Case
    val quantity: Quantity
    val animacy: Boolean
    val source: NameSource
}
