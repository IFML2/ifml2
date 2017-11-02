package ifml2.vm

import ifml2.vm.values.EmptyValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VariableMapTest {
    @Test
    fun testClear() {
        // Given
        val vm: VariableMap = VariableMapImpl()
        vm.put(Variable("one", EmptyValue()))
        vm.put(Variable("two", EmptyValue()))
        vm.put(Variable("three", EmptyValue()))

        // When
        vm.clear()

        // Then
        Assertions.assertEquals(0, vm.variables.size)
    }

    @Test
    fun testPutAndGet() {
        // Given
        val name = "TheName"
        val variable = Variable(name, EmptyValue())
        val vm: VariableMap = VariableMapImpl()
        vm.put(variable)

        // When
        val empty = vm.get(name)

        // Then
        Assertions.assertEquals(variable, empty)
    }
}