package ifml2.grammar.word

import ifml2.grammar.source.NounSource
import ifml2.grammar.word.parts.Case
import ifml2.grammar.word.parts.Gender
import ifml2.grammar.word.parts.Quantity

data class Noun(
        override val name: String,
        override val gender: Gender,
        override val case: Case,
        override val quantity: Quantity,
        override val animacy: Boolean,
        override val source: NounSource
) : Name {
}
