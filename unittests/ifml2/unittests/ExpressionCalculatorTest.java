package ifml2.unittests;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.om.Location;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2ExpressionException;
import ifml2.vm.SymbolResolver;
import ifml2.vm.values.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

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
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));
        when(mockSymbolResolver.resolveSymbol("нет")).thenReturn(new BooleanValue(false));
    }

    private static boolean extractBoolean(Value result) {
        boolean resultIsBooleanValue;
        BooleanValue booleanValue;
        Boolean value;
        resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);
        booleanValue = (BooleanValue) result;
        value = booleanValue.getValue();
        return value;
    }

    private static Value calculate(String expression) throws IFML2Exception {
        return ExpressionCalculator.calculate(mockSymbolResolver, expression);
    }

    // 1 + 1
    @Test
    public void onePlusOne() throws IFML2Exception
    {
        Value result = calculate("1 + 1");

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
        Value result = calculate("не да");
        assertFalse(extractBoolean(result));
    }

    // не не да
    @Test
    public void notNotYes() throws IFML2Exception
    {
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));

        Value result = calculate(mockSymbolResolver, "не не да");
        assertTrue(extractBoolean(result));
    }

    // локация.свойство
    @Test
    public void locationDotProperty() throws IFML2Exception
    {
        final Location mockLocation = mock(Location.class);
        when(mockSymbolResolver.resolveSymbol("локация")).thenReturn(new ObjectValue(mockLocation));
        when(mockLocation.getMemberValue(eq("свойство"), Matchers.<SymbolResolver>any())).thenReturn(new TextValue("значение"));

        Value result = calculate("локация.свойство");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue("Тип результата - не TextValue", resultIsTextValue);

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("значение", value);
    }

    // объект.свойство
    @Test
    public void objectDotProperty() throws IFML2Exception
    {
        final IFMLObject mockObject = mock(IFMLObject.class);
        when(mockSymbolResolver.resolveSymbol("объект")).thenReturn(new ObjectValue(mockObject));
        when(mockObject.getMemberValue(eq("свойство"), Matchers.<SymbolResolver>any())).thenReturn(new TextValue("значение"));

        Value result = calculate("объект.свойство");

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
        Value result = calculate("1 = 1");
        assertTrue(extractBoolean(result));
    }

    // "Номер " + 1
    @Test
    public void stringPlusOne() throws IFML2Exception
    {
        Value result = calculate("\"Номер \" + 1");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue("Тип результата - не TextValue", resultIsTextValue);

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("Номер 1", value);
    }

    // 1 < 2
    public void testCalculateOneLesserThanTwo() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "1 < 2");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // 1 > 2
    public void testCalculateOneGreaterThanTwo() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "1 > 2");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertFalse(value);
    }

    // 1 <> 2
    public void testCalculateOneNotEqualsTwo() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "1 <> 2");

        boolean resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue("Тип результата - не BooleanValue", resultIsBooleanValue);

        BooleanValue booleanValue = (BooleanValue) result;
        Boolean value = booleanValue.getValue();
        assertTrue(value);
    }

    // 1 - 2
    public void testCalculateOneMinusTwo() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "1 - 2");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(-1., value);
    }

    // -1
    public void testCalculateNegativeOne() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "-1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(-1., value);
    }

    // 2 + -1
    public void testCalculateTwoMinusNegativeOne() throws IFML2Exception
    {
        Value result = calculate(mockSymbolResolver, "2 + -1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue("Тип результата - не NumberValue", resultIsNumberValue);

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(1., value);
    }

    // операции с И
    @Test
    public void andOperations() throws IFML2Exception
    {
        Value result;

        // да и да
        result = calculate("да и да");
        assertTrue(extractBoolean(result));

        // да и нет
        result = calculate("да и нет");
        assertFalse(extractBoolean(result));

        // нет и да
        result = calculate("нет и да");
        assertFalse(extractBoolean(result));

        // нет и нет
        result = calculate("нет и нет");
        assertFalse(extractBoolean(result));
    }

    // операции с ИЛИ
    @Test
    public void orOperations() throws IFML2Exception
    {
        Value result;

        // да или да
        result = calculate("да или да");
        assertTrue(extractBoolean(result));

        // да или нет
        result = calculate("да или нет");
        assertTrue(extractBoolean(result));

        // нет или да
        result = calculate("нет или да");
        assertTrue(extractBoolean(result));

        // нет или нет
        result = calculate("нет или нет");
        assertFalse(extractBoolean(result));
    }

    // нет и НеизвестныйИд
    @Test
    public void noAndUnknownId() throws IFML2Exception
    {
        String expression = "нет и НеизвестныйИд";
        Value result = calculate(expression);
        assertFalse(extractBoolean(result));
    }

    // муть должна падать
    @Test(expected = IFML2ExpressionException.class)
    public void wrongExpression() throws IFML2Exception
    {
        calculate("нет и gfdg fg9 gjdfg");
    }
}
