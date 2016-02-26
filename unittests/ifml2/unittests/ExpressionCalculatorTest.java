package ifml2.unittests;

import ifml2.IFML2Exception;
import ifml2.om.Location;
import ifml2.vm.IFML2ExpressionException;
import ifml2.vm.SymbolResolver;
import ifml2.vm.values.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import static ifml2.vm.ExpressionCalculator.calculate;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExpressionCalculatorTest
{
    private static SymbolResolver mockSymbolResolver;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception
    {
        mockSymbolResolver = mock(SymbolResolver.class);
    }

    // 1 + 1
    @Test
    public void onePlusOne() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "1 + 1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(2., value, 0);
    }

    // не да
    @Test
    public void notYes() throws IFML2Exception
    {
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));

        Value result = calculate(mockSymbolResolver, "не да");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertFalse(value);
    }

    // локация.свойство
    @Test
    public void locationDotProperty() throws IFML2Exception
    {
        Location mockLocation = mock(Location.class);
        when(mockSymbolResolver.resolveSymbol("локация")).thenReturn(new ObjectValue(mockLocation));
        when(mockLocation.getMemberValue(eq("свойство"), Matchers.<SymbolResolver>any())).thenReturn(new TextValue("значение"));

        Value result = calculate(mockSymbolResolver, "локация.свойство");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue("Тип результата - не TextValue", resultIsTextValue);

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("значение", value);
    }

    // 1 = 1
    @Test
    public void oneEqualsOne() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "1 = 1");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // "Номер " + 1
    @Test
    public void stringPlusOne() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "\"Номер \" + 1");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue("Тип результата - не TextValue", resultIsTextValue);

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("Номер 1", value);
    }

    // операции с И
    @Test
    public void andOperations() throws IFML2Exception
    {
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));
        when(mockSymbolResolver.resolveSymbol("нет")).thenReturn(new BooleanValue(false));

        Value result;
        boolean resultIsBooleanValue;
        BooleanValue booleanValue;
        Boolean value;

        // да и да
        result = calculate(mockSymbolResolver, "да и да");
        resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);
        booleanValue = (BooleanValue) result;
        value = booleanValue.getValue();
        assertTrue(value);

        // да и нет
        result = calculate(mockSymbolResolver, "да и нет");
        resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);
        booleanValue = (BooleanValue) result;
        value = booleanValue.getValue();
        assertFalse(value);

        // нет и да
        result = calculate(mockSymbolResolver, "нет и да");
        resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);
        booleanValue = (BooleanValue) result;
        value = booleanValue.getValue();
        assertFalse(value);

        // нет и нет
        result = calculate(mockSymbolResolver, "нет и нет");
        resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);
        booleanValue = (BooleanValue) result;
        value = booleanValue.getValue();
        assertFalse(value);
    }

    // нет и НеизвестныйИд
    @Test
    public void noAndUnknownId() throws IFML2Exception
    {
        when(mockSymbolResolver.resolveSymbol("нет")).thenReturn(new BooleanValue(false));

        Value result = calculate(mockSymbolResolver, "нет и НеизвестныйИд");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertFalse(value);
    }

    // муть должна падать
    @Test(expected = IFML2ExpressionException.class)
    public void wrongExpression() throws IFML2Exception
    {
        calculate(mockSymbolResolver, "нет и gfdg fg9 gjdfg");
    }
}
