package ifml2.grammar.dictionary

import ifml2.grammar.word.Noun
import ifml2.grammar.word.parts.Case.*
import ifml2.grammar.word.parts.Gender.MALE
import ifml2.grammar.source.NounSource
import ifml2.grammar.word.parts.Quantity.SINGULAR
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SourceDictionaryTest {

    @Test
    fun testNaiveSourceDictionary() {
        // Given
        val ivanSource = NounSource("ваня", mapOf(
                NOMINATIVE to "ваня", // иминительный
                GENITIVE to "ваню", // родительный
                DATIVE to "ване", // датильный
                ACCUSATIVE to "ваню", // винительный
                INSTRUMENTAL to "ваней", // творительный
                PREPOSITIONAL to "ване", // предложный
                VOCATIVE to "вань"       // звательный
        ), MALE, SINGULAR, true)
        val dictionary = NaiveSourceDictionary()
        dictionary.addSource(ivanSource)
        val text = "ваню"

        // When
        val result = dictionary.find(text)

        // Then
        Assertions.assertEquals(result.size, 2)
        result.forEach {
            Assertions.assertEquals(it.name, text)
            val noun = it as Noun
            Assertions.assertEquals(noun.gender, MALE)
            Assertions.assertEquals(noun.quantity, SINGULAR)
            Assertions.assertTrue(noun.animacy)
        }
        Assertions.assertEquals((result[0] as Noun).case, GENITIVE)
        Assertions.assertEquals((result[1] as Noun).case, ACCUSATIVE)
        val noun = result[0] as Noun
        Assertions.assertEquals(noun.source?.asCase(GENITIVE)?.name, noun.name)
    }
}
