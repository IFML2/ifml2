package ifml2.vm.value

import ifml2.om.IFMLObject
import ifml2.vm.values.*
import ifml2.vm.values.CompareResult.EQUAL
import ifml2.vm.values.CompareResult.UNEQUAL
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNull

class ValueTest {

    @Test
    fun testEmptyValues() {
        // Given
        val emptyVal = EmptyValue()

        // Then
        assertEquals(EmptyValue.LITERAL, emptyVal.toLiteral())
        assertEquals(EmptyValue.LITERAL, emptyVal.typeName)
        assertEquals(EQUAL, emptyVal.compareTo(EmptyValue()))
        assertEquals(EQUAL, emptyVal.compareTo(emptyVal))
    }

    @Test
    fun testBooleanValues() {
        // Given
        val trueVal = BooleanValue(true)
        val falseVal = BooleanValue(false)

        // Then
        assertEquals(BooleanValue.TRUE, trueVal.toLiteral())
        assertEquals(BooleanValue.FALSE, falseVal.toLiteral())
        assertEquals(BooleanValue.LITERAL, trueVal.typeName)
        assertEquals(BooleanValue.LITERAL, falseVal.typeName)
        assertEquals(EQUAL, trueVal.compareTo(trueVal))
        assertEquals(UNEQUAL, trueVal.compareTo(falseVal))
        assertEquals(EQUAL, falseVal.compareTo(falseVal))
    }

    @Test
    fun testTextValues() {
        // Given
        val oneVal = TextValue("One")
        val twoVal = TextValue("Two")

        // When
        val textRes = oneVal + twoVal

        // Then
        assertEquals(TextValue.LITERAL, oneVal.typeName)
        assertEquals("'One'", oneVal.toLiteral())
        assertEquals("'Two'", twoVal.toLiteral())
        assertEquals("'OneTwo'", textRes.toLiteral())
        assertEquals(UNEQUAL, oneVal.compareTo(twoVal))
        assertEquals(EQUAL, TextValue("One").compareTo(oneVal))
    }

    @Test
    fun testNumberValues() {
        // Given
        val num1Val = NumberValue(1.0)
        val num2Val = NumberValue(2.0)

        // When
        val numSum = num1Val + num2Val
        val numDup = num1Val + num1Val
        val mixVal = num2Val + TextValue("EM")

        // Then
        assertEquals(NumberValue.LITERAL, num1Val.typeName)
        assertEquals(NumberValue.LITERAL, numDup.typeName)
        assertEquals("2", num2Val.toLiteral())
        assertEquals(3.0, numSum.value)
        assertEquals(2.0, numDup.value)
        assertEquals(num2Val, numDup)
        assertEquals("'2EM'", mixVal.toLiteral())
        assertEquals(CompareResult.LEFT_BIGGER, num2Val.compareTo(num1Val))
        assertEquals(CompareResult.RIGHT_BIGGER, num1Val.compareTo(num2Val))
        assertEquals(CompareResult.EQUAL, num2Val.compareTo(numDup))
        assertEquals(CompareResult.NOT_APPLICABLE, num2Val.compareTo(TextValue("2.0")))
    }

    @Test
    fun testObjectValues() {
        // Given
        val obj = IFMLObject()

        // When
        val objVal = ObjectValue(obj)

        // Then
        assertNull(objVal.toLiteral())
        assertEquals(ObjectValue.LITERAL, objVal.typeName)
    }

    @Test
    fun testSymbolValues() {
        // Given
        val ident = "Ident"

        // When
        val symVal = SymbolValue(ident)

        // Then
        assertEquals(ident, symVal.toLiteral())
        assertEquals(SymbolValue.LITERAL, symVal.typeName)
    }

    @Test
    fun testCollectionValue() {
        // Given
        val colVal = CollectionValue(Collections.emptyList())

        // When

        // Then
        assertEquals(CollectionValue.LITERAL, colVal.typeName)
        assertEquals("CollectionValue", colVal.toString())
    }
}