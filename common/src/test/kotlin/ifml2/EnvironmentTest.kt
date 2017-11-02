package ifml2

import ifml2.engine.featureproviders.graphic.OutputIconProvider
import ifml2.engine.featureproviders.text.OutputPlainTextProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.swing.Icon

class DummyTextProvider() : OutputPlainTextProvider {
    var text: String = ""
    override fun outputPlainText(text: String) {
        this.text = text
    }
}

class DummyIconProvider() : OutputIconProvider {
    override fun outputIcon(icon: Icon) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class EnvironmentTest {

    @Test
    fun testOutText() {
        // Given
        val textProv = DummyTextProvider()
        val env: Environment = EnvironmentImpl(textProv, DummyIconProvider())

        // When
        env.outText("The text")

        // Then
        Assertions.assertEquals("The text", textProv.text)
    }
}