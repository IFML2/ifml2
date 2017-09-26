package ifml2.grammar.dictionary

import ifml2.grammar.word.Particle
import ifml2.grammar.word.Unknown
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DictionaryTest {
    @Test
    fun testEmptyDictionary() {
        // Given
        val word: String = "слово"
        val dictionary: Dictionary = EmptyDictionary()
        // When
        val result = dictionary.find(word)
        // Then
        Assertions.assertEquals(result.size, 1)
        Assertions.assertEquals(result[0], Unknown(word))
    }

    @Test
    fun testNaiveDictionary() {
        // Given
        val word: String = "ли"
        val dictionary: Dictionary = NaiveDictionary()
        // When
        dictionary.add(Particle(word))
        val result = dictionary.find(word)
        // Then
        Assertions.assertEquals(result.size, 1)
        Assertions.assertEquals(result[0], Particle(word))
    }
}
