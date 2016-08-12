package ifml2.unittests;

import ifml2.IFML2Exception;
import ifml2.om.Location;
import ifml2.vm.ISymbolResolver;
import ifml2.vm.values.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static ifml2.vm.ExpressionCalculator.calculate;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExpressionCalculatorTest {
    private ISymbolResolver mockSymbolResolver;

    @Before
    public void setUp() throws Exception {
        mockSymbolResolver = mock(ISymbolResolver.class);
    }

    // 1 + 1
    @Test
    public void testCalculateOnePlusOne() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "1 + 1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(new Double(2.), value);
    }

    // не да
    @Test
    public void testCalculateNotYes() throws IFML2Exception {
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));

        Value result = calculate(mockSymbolResolver, "не да");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertFalse(value);
    }

    // не не да
    @Test
    public void testCalculateNotNotYes() throws IFML2Exception {
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));

        Value result = calculate(mockSymbolResolver, "не не да");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // локация.свойство
    @Test
    public void testCalculateLocationDotProperty() throws IFML2Exception {
        Location mockLocation = mock(Location.class);
        when(mockSymbolResolver.resolveSymbol("локация")).thenReturn(new ObjectValue(mockLocation));
        when(mockLocation.getMemberValue(eq("свойство"), ArgumentMatchers.any())).thenReturn(new TextValue("значение"));

        Value result = calculate(mockSymbolResolver, "локация.свойство");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue("Тип результата - не TextValue", resultIsTextValue);

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("значение", value);
    }

    // 1 = 1
    @Test
    public void testCalculateOneEqualsOne() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "1 = 1");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // "Номер " + 1
    @Test
    public void testCalculateStringPlusOne() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "\"Номер \" + 1");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue("Тип результата - не TextValue", resultIsTextValue);

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("Номер 1", value);
    }

    // 1 < 2
    @Test
    public void testCalculateOneLesserThanTwo() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "1 < 2");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // 1 > 2
    @Test
    public void testCalculateOneGreaterThanTwo() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "1 > 2");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertFalse(value);
    }

    // 1 <> 2
    @Test
    public void testCalculateOneNotEqualsTwo() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "1 <> 2");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // 1 - 2
    @Test
    public void testCalculateOneMinusTwo() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "1 - 2");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(new Double(-1.), value);
    }

    // -1
    @Test
    public void testCalculateNegativeOne() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "-1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(new Double(-1.), value);
    }

    // 2 + -1
    @Test
    public void testCalculateTwoMinusNegativeOne() throws IFML2Exception {
        Value result = calculate(mockSymbolResolver, "2 + -1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(new Double(1.), value);
    }
}
