package ifml2.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class HistoryServiceTest {

    @Test
    fun prev() {
        val history = HistoryService()
        history.update("one")
        history.update("two")
        history.update("three")

        Assertions.assertEquals("three", history.prev())
        Assertions.assertEquals("two", history.prev())
        Assertions.assertEquals("one", history.prev())
        Assertions.assertTrue(history.prev().isEmpty())
    }

    @Test
    operator fun next() {
        val history = HistoryService()
        history.update("one")
        history.update("two")
        history.update("three")

        history.prev()
        history.prev()
        history.prev()
        Assertions.assertEquals("one", history.next())
        Assertions.assertEquals("two", history.next())
        Assertions.assertEquals("three", history.next())
        Assertions.assertTrue(history.next().isEmpty())
    }

}