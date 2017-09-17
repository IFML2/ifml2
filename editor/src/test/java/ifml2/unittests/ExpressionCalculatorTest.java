package ifml2.unittests;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.om.Location;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2ExpressionException;
import ifml2.vm.SymbolResolver;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.NumberValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExpressionCalculatorTest {
    private static SymbolResolver mockSymbolResolver;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        mockSymbolResolver = mock(SymbolResolver.class);
        when(mockSymbolResolver.resolveSymbol("да")).thenReturn(new BooleanValue(true));
        when(mockSymbolResolver.resolveSymbol("нет")).thenReturn(new BooleanValue(false));
    }

    private static boolean extractBoolean(Value result) {
        boolean resultIsBooleanValue;
        BooleanValue booleanValue;
        Boolean value;
        resultIsBooleanValue = result instanceof BooleanValue;
        assertTrue(resultIsBooleanValue, "Тип результата - не BooleanValue");
        booleanValue = (BooleanValue) result;
        value = booleanValue.getValue();
        return value;
    }

    private static Value calculate(String expression) throws IFML2Exception {
        return ExpressionCalculator.calculate(mockSymbolResolver, expression);
    }

    // 1 + 1 => 2
    @Test
    public void onePlusOne() throws IFML2Exception {
        Value result = calculate("1 + 1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue(resultIsNumberValue, "Тип результата - не NumberValue");

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(2., value, 0);
    }

    // не да => нет
    @Test
    public void notYes() throws IFML2Exception {
        Value result = calculate("не да");
        assertFalse(extractBoolean(result));
    }

    // не не да => да
    @Test
    public void notNotYes() throws IFML2Exception {
        Value result = calculate("не не да");
        assertTrue(extractBoolean(result));
    }

    // да и не нет и да => да
    @Test
    public void yesAndNotNoAndYes() throws IFML2Exception {
        Value result = calculate("да и не нет и да");
        assertTrue(extractBoolean(result));
    }

    // да и не да и да => нет
    @Test
    public void yesAndNotYesAndYes() throws IFML2Exception {
        Value result = calculate("да и не да и да");
        assertFalse(extractBoolean(result));
    }

    // да и не да и не не не да => нет
    @Test
    public void yesAndNotYesAndNotNotNotYes() throws IFML2Exception {
        Value result = calculate("да и не да и не не не да");
        assertFalse(extractBoolean(result));
    }

    // локация.свойство
    @Test
    public void locationDotProperty() throws IFML2Exception {
        final Location mockLocation = mock(Location.class);
        when(mockSymbolResolver.resolveSymbol("локация")).thenReturn(new ObjectValue(mockLocation));
        when(mockLocation.getMemberValue(eq("свойство"), Matchers.<SymbolResolver>any())).thenReturn(new TextValue("значение"));

        Value result = calculate("локация.свойство");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue(resultIsTextValue, "Тип результата - не TextValue");

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("значение", value);
    }

    // объект.свойство
    @Test
    public void objectDotProperty() throws IFML2Exception {
        final IFMLObject mockObject = mock(IFMLObject.class);
        when(mockSymbolResolver.resolveSymbol("объект")).thenReturn(new ObjectValue(mockObject));
        when(mockObject.getMemberValue(eq("свойство"), Matchers.<SymbolResolver>any())).thenReturn(new TextValue("значение"));

        Value result = calculate("объект.свойство");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue(resultIsTextValue, "Тип результата - не TextValue");

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("значение", value);
    }

    // 1 = 1 => да
    @Test
    public void oneEqualsOne() throws IFML2Exception {
        Value result = calculate("1 = 1");
        assertTrue(extractBoolean(result));
    }

    // "Номер " + 1 => "Номер 1"
    @Test
    public void stringPlusOne() throws IFML2Exception {
        Value result = calculate("\"Номер \" + 1");

        boolean resultIsTextValue = result instanceof TextValue;
        assertTrue(resultIsTextValue, "Тип результата - не TextValue");

        TextValue textValue = (TextValue) result;
        String value = textValue.getValue();
        assertEquals("Номер 1", value);
    }

    // 1 < 2 => да
    @Test
    public void oneLesserThanTwo() throws IFML2Exception {
        Value result = calculate("1 < 2");
        assertTrue(extractBoolean(result));
    }

    // 1 > 2 => нет
    @Test
    public void oneGreaterThanTwo() throws IFML2Exception {
        Value result = calculate("1 > 2");
        assertFalse(extractBoolean(result));
    }

    // 1 <> 2 => да
    @Test
    public void oneNotEqualsTwo() throws IFML2Exception {
        Value result = calculate("1 <> 2");
        assertTrue(extractBoolean(result));
    }

    // 1 - 2 => -1
    @Test
    public void oneMinusTwo() throws IFML2Exception {
        Value result = calculate("1 - 2");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue(resultIsNumberValue, "Тип результата - не NumberValue");

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(-1., value, 0);
    }

    // -1 => -1
    @Test
    public void negativeOne() throws IFML2Exception {
        Value result = calculate("-1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue(resultIsNumberValue, "Тип результата - не NumberValue");

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(-1., value, 0);
    }

    // 2 + -1 => 1
    @Test
    public void twoMinusNegativeOne() throws IFML2Exception {
        Value result = calculate("2 + -1");

        boolean resultIsNumberValue = result instanceof NumberValue;
        assertTrue(resultIsNumberValue, "Тип результата - не NumberValue");

        NumberValue numberValue = (NumberValue) result;
        Double value = numberValue.getValue();
        assertEquals(1., value, 0);
    }

    // операции с И
    @Test
    public void andOperations() throws IFML2Exception {
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
    public void orOperations() throws IFML2Exception {
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

    // нет и НеизвестныйИд => нет // TODO: 07.03.2016 Нужно как-то проверять корректность выражений, например, все SymbolValue пытаться резолвить, если они не правая часть в DotExpression
    @Test
    public void noAndUnknownId() throws IFML2Exception {
        String expression = "нет и НеизвестныйИд";
        Value result = calculate(expression);
        assertFalse(extractBoolean(result));
    }

    // чушь должна падать
    @Test
    public void wrongExpression() throws IFML2Exception {
        Throwable exceptioin = assertThrows(IFML2ExpressionException.class, () -> {
            calculate("нет и bull3 fg9 blah4");
        });
        assertTrue(exceptioin.getMessage().startsWith("Ошибка в выражении"));
    }

    // элемент в коллекция => да
    @Test
    public void inExpression() throws IFML2Exception {
        IFMLObject ifmlObject = mock(IFMLObject.class);
        CollectionValue collectionValue = new CollectionValue(Collections.singletonList(ifmlObject));
        when(mockSymbolResolver.resolveSymbol("элемент")).thenReturn(new ObjectValue(ifmlObject));
        when(mockSymbolResolver.resolveSymbol("коллекция")).thenReturn(collectionValue);

        String expression = "элемент в коллекция";
        Value result = calculate(expression);
        assertTrue(extractBoolean(result));
    }
}
