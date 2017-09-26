package ifml2.grammar.source

import ifml2.grammar.word.*
import ifml2.grammar.word.parts.Case
import ifml2.grammar.word.parts.Gender
import ifml2.grammar.word.parts.Quantity

class NounSource(
        override val name: String,
        override val cases: Map<Case, String>,
        override val gender: Gender,
        override val quantity: Quantity,
        override val animacy: Boolean
) : NameSource {

    override fun asCase(case: Case): Name {
        val cname = cases[case]
        return Noun(cname ?: name, gender, case, quantity, animacy, this)
    }

}
