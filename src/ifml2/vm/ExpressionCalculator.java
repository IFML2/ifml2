package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.vm.values.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Stack;

import static ifml2.vm.ExpressionCalculator.ExprContext.*;

public class ExpressionCalculator
{
    private static final char GET_PROPERTY_OPERATOR = '.';
    private static final char EQUALITY_OPERATOR = '=';
    private static final char ADD_OPERATOR = '+';
    private static final String NOT_OPERATOR = "не";
    private static final String AND_OPERATOR = "и";
    private static final String OR_OPERATOR = "или";
    private static final String IN_OPERATOR = "в";

    private static final char QUOTE_CHAR = '"';
    private static final char SINGLE_QUOTE_CHAR = '\'';

    private ISymbolResolver symbolResolver = null;

    private ExpressionCalculator(ISymbolResolver symbolResolver)
    {
        this.symbolResolver = symbolResolver;
    }

    public static Value calculate(@NotNull ISymbolResolver symbolResolver, String expression) throws IFML2Exception
    {
        ExpressionCalculator expressionCalculator = new ExpressionCalculator(symbolResolver);
        return expressionCalculator.calculate(expression);
    }

    Value calculate(String expression) throws IFML2Exception
    {
        String exp = expression.trim();

        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(exp));
        tokenizer.parseNumbers();
        tokenizer.commentChar(0);
        tokenizer.quoteChar(QUOTE_CHAR);
        tokenizer.quoteChar(SINGLE_QUOTE_CHAR);
        for (ExpressionOperator expressionOperator : ExpressionOperator.values())
        {
            if (expressionOperator.operatorCharacter != 0)
            {
                tokenizer.ordinaryChar(expressionOperator.operatorCharacter);
            }
        }

        CalculationStack calculationStack = new CalculationStack();

        int token;
        ExprContext context = START;
        try
        {
            while ((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)
            {
                switch (token)
                {
                    case StreamTokenizer.TT_WORD:
                        if (NOT_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            if (OPERAND.equals(context))
                            {
                                // NOT can't follow operand!
                                throw new IFML2ExpressionException("Ошибка в выражении: НЕ не может следовать за операндом");
                            }
                            calculationStack.pushOperator(ExpressionOperator.NOT);
                            context = OPERATOR;
                        }
                        else if (AND_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(ExpressionOperator.AND);
                                    context = OPERATOR;
                                    break;
                                default:
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        }
                        else if (OR_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(ExpressionOperator.OR);
                                    context = OPERATOR;
                                    break;
                                default:
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        }
                        else if (IN_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(ExpressionOperator.IN);
                                    context = OPERATOR;
                                    break;
                                default:
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        }
                        else
                        {
                            if (OPERAND.equals(context))
                            {
                                throw new IFML2ExpressionException(
                                        "Ошибка в выражении: идентификатор ({0}) не может следовать за другим операндом", tokenizer.sval);
                            }
                            calculationStack.pushSymbol(tokenizer.sval);
                            context = OPERAND;
                        }
                        break;

                    case StreamTokenizer.TT_NUMBER:
                        if (OPERAND.equals(context))
                        {
                            throw new IFML2ExpressionException("Ошибка в выражении: число ({0}) не может следовать за другим операндом",
                                    tokenizer.nval);
                        }
                        calculationStack.pushNumericLiteral(tokenizer.nval);
                        context = OPERAND;
                        break;

                    case QUOTE_CHAR:
                    case SINGLE_QUOTE_CHAR:
                        if (OPERAND.equals(context))
                        {
                            throw new IFML2ExpressionException("Ошибка в выражении: текст ({0}) не может следовать за другим операндом",
                                    tokenizer.sval);
                        }
                        calculationStack.pushTextLiteral(tokenizer.sval);
                        context = OPERAND;
                        break;

                    case GET_PROPERTY_OPERATOR:
                        calculationStack.pushOperator(ExpressionOperator.GET_PROPERTY);
                        context = OPERATOR;
                        break;

                    case EQUALITY_OPERATOR:
                        calculationStack.pushOperator(ExpressionOperator.COMPARE_EQUALITY);
                        context = OPERATOR;
                        break;

                    case ADD_OPERATOR:
                        calculationStack.pushOperator(ExpressionOperator.ADD);
                        context = OPERATOR;
                        break;

                    default:
                        throw new IFML2ExpressionException("Ошибка в выражении ({0}) - неизвестный токен ({1})", expression, token);
                }
            }
        }
        catch (IOException e)
        {
            throw new IFML2Exception(e);
        }

        calculationStack.solve();

        if (calculationStack.valueStack.size() == 0)
        {
            throw new IFML2VMException("Стек вычислений пуст!");
        }

        if (calculationStack.valueStack.size() > 1)
        {
            throw new IFML2VMException("В стеке вычислений больше одного значения ({0})!", calculationStack.valueStack);
        }

        return calculationStack.valueStack.pop();
    }

    private Value resolveSymbol(UnresolvedSymbolValue unresolvedSymbolValue) throws IFML2VMException
    {
        String symbol = unresolvedSymbolValue.getValue().trim();
        return symbolResolver.resolveSymbol(symbol);
    }

    /**
     * What is just happened
     */
    enum ExprContext
    {
        START,
        OPERATOR,
        OPERAND
    }

    private enum OperatorType
    {
        BINARY,
        UNARY_RIGHT
    }

    private enum ExpressionOperator
    {
        GET_PROPERTY(GET_PROPERTY_OPERATOR, 100),
        IN(IN_OPERATOR, OperatorType.BINARY, 50),
        NOT(NOT_OPERATOR, OperatorType.UNARY_RIGHT, 40),
        ADD(ADD_OPERATOR, 30),
        COMPARE_EQUALITY(EQUALITY_OPERATOR, 20),
        AND(AND_OPERATOR, OperatorType.BINARY, 10),
        OR(OR_OPERATOR, OperatorType.BINARY, 5);

        public final char operatorCharacter;
        public final int priority;
        public String operatorString;
        private OperatorType operatorType = OperatorType.BINARY;

        ExpressionOperator(char operatorCharacter, int priority)
        {
            this.operatorCharacter = operatorCharacter;
            this.priority = priority;
        }

        ExpressionOperator(String operatorString, OperatorType operatorType, int priority)
        {
            this((char) 0, priority);
            this.operatorString = operatorString;
            this.operatorType = operatorType;
        }
    }

    private class CalculationStack
    {
        final Stack<Value> valueStack = new Stack<Value>();
        final Stack<ExpressionOperator> operatorStack = new Stack<ExpressionOperator>();

        public void pushSymbol(String symbol)
        {
            valueStack.push(new UnresolvedSymbolValue(symbol));
        }

        public void pushTextLiteral(String text)
        {
            valueStack.push(new TextValue(text));
        }

        public void pushNumericLiteral(double number)
        {
            valueStack.push(new NumberValue(number));
        }

        public void pushOperator(ExpressionOperator operator) throws IFML2Exception
        {
            operatorStack.push(operator);
            shrink();
        }

        private void shrink() throws IFML2Exception
        {
            while (operatorStack.size() >= 2)
            {
                ExpressionOperator lastOperator = operatorStack.pop();
                ExpressionOperator prevOperator = operatorStack.pop();

                if (firstHasLessOrEqPriority(lastOperator, prevOperator))
                {
                    Value operationResult = doOperation(prevOperator);

                    valueStack.push(operationResult);
                    operatorStack.push(lastOperator);
                }
                else
                {
                    operatorStack.push(prevOperator);
                    operatorStack.push(lastOperator);

                    break;
                }
            }
        }

        private Value doOperation(ExpressionOperator operator) throws IFML2Exception
        {
            Value result;

            Value rightValue;
            Value leftValue = null;

            switch (operator.operatorType)
            {
                case BINARY:
                    rightValue = valueStack.pop();
                    leftValue = valueStack.pop();
                    break;

                case UNARY_RIGHT:
                    rightValue = valueStack.pop();
                    break;

                default:
                    throw new IFML2ExpressionException("Неизвестный тип операции {0}", operator.operatorType);
            }

            switch (operator)
            {
                case GET_PROPERTY:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    if (!(resolvedLeftValue instanceof ObjectValue))
                    {
                        throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                    }

                    if (!(rightValue instanceof UnresolvedSymbolValue))
                    {
                        throw new IFML2ExpressionException("Величина не является именем свойства ({0})", rightValue);
                    }

                    IFMLObject object = ((ObjectValue) resolvedLeftValue).getValue();
                    if (object == null)
                    {
                        throw new IFML2ExpressionException("Объект {0} не задан (пуст)", leftValue);
                    }

                    String propertyName = ((UnresolvedSymbolValue) rightValue).getValue();
                    Value propertyValue = object.getMemberValue(propertyName, symbolResolver);
                    result = propertyValue == null ? new EmptyValue() : propertyValue;

                    break;
                }

                case COMPARE_EQUALITY:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    assert resolvedLeftValue != null;

                    Value.CompareResult compareResult = resolvedLeftValue.compareTo(resolvedRightValue);
                    switch (compareResult)
                    {
                        case EQUAL:
                            result = new BooleanValue(true);
                            break;
                        case UNEQUAL:
                        case LEFT_BIGGER:
                        case RIGHT_BIGGER:
                            result = new BooleanValue(false);
                            break;
                        case NOT_APPLICABLE:
                            throw new IFML2ExpressionException("Сравниваемые величины разного типа ({0} и {1})", leftValue, rightValue);

                        default:
                            throw new IFML2VMException("Неизвестный результат сравнения величин ({0})", compareResult);
                    }

                    break;
                }

                case ADD:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    assert resolvedLeftValue != null;

                    if (resolvedLeftValue instanceof IAddableValue)
                    {
                        result = ((IAddableValue) resolvedLeftValue).add(resolvedRightValue);
                    }
                    else if (resolvedRightValue instanceof TextValue)
                    {
                        /*
                        если левый операнд не поддерживает сложение, но правый - текст, то всё превращаем в строку и клеим
                         */
                        result = new TextValue(resolvedLeftValue.toString() + ((TextValue) resolvedRightValue).getValue());
                    }
                    else
                    {
                        throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"",
                                Value.Operation.ADD, resolvedLeftValue.getTypeName(), resolvedLeftValue.getTypeName());
                    }

                    break;
                }

                case AND:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    if (!(resolvedLeftValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", leftValue);
                    }
                    if (!(resolvedRightValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                    }

                    boolean leftBoolValue = ((BooleanValue) resolvedLeftValue).getValue();
                    boolean rightBoolValue = ((BooleanValue) resolvedRightValue).getValue();

                    result = new BooleanValue(leftBoolValue && rightBoolValue);

                    break;
                }

                case OR:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    if (!(resolvedLeftValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", leftValue);
                    }
                    if (!(resolvedRightValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                    }

                    boolean leftBoolValue = ((BooleanValue) resolvedLeftValue).getValue();
                    boolean rightBoolValue = ((BooleanValue) resolvedRightValue).getValue();

                    result = new BooleanValue(leftBoolValue || rightBoolValue);

                    break;
                }

                case NOT:
                {
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    if (!(resolvedRightValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                    }

                    boolean booleanValue = ((BooleanValue) resolvedRightValue).getValue();

                    result = new BooleanValue(!booleanValue);

                    break;
                }

                case IN:
                {
                    Value preparedLeftValue = ensureValueResolved(leftValue);
                    Value preparedRightValue = ensureValueResolved(rightValue);

                    if (!(preparedLeftValue instanceof ObjectValue))
                    {
                        throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                    }

                    if (!(preparedRightValue instanceof CollectionValue))
                    {
                        throw new IFML2ExpressionException("Величина не является коллекцией ({0})", rightValue);
                    }

                    ObjectValue objectValue = (ObjectValue) preparedLeftValue;
                    CollectionValue collectionValue = (CollectionValue) preparedRightValue;
                    result = new BooleanValue(collectionValue.getValue().contains(objectValue.getValue()));

                    break;
                }

                default:
                {
                    throw new IFML2ExpressionException("Неизвестный оператор {0}", operator);
                }
            }

            return result;
        }

        private Value ensureValueResolved(Value unpreparedValue) throws IFML2VMException
        {
            Value preparedValue;

            if (unpreparedValue instanceof UnresolvedSymbolValue)
            {
                preparedValue = resolveSymbol((UnresolvedSymbolValue) unpreparedValue);
            }
            else
            {
                preparedValue = unpreparedValue;
            }

            return preparedValue;
        }

        private boolean firstHasLessOrEqPriority(ExpressionOperator firstOperator, ExpressionOperator secondOperator)
        {
            return firstOperator.priority <= secondOperator.priority;
        }

        public void solve() throws IFML2Exception
        {
            shrink();

            if (!operatorStack.isEmpty() && valueStack.size() < 1)
            {
                throw new IFML2ExpressionException("Системный сбой: стек операторов не пуст или недостаточно значений в стеке значений!" +
                                                   "\nСтек операторов: {0}\nСтек значений: {1}", operatorStack, valueStack);
            }

            while (!operatorStack.isEmpty())
            {
                ExpressionOperator operator = operatorStack.pop();

                Value operationResult = doOperation(operator);

                valueStack.push(operationResult);
            }

            if (valueStack.size() == 1)
            {
                Value lastValue = valueStack.pop();

                lastValue = ensureValueResolved(lastValue);
                valueStack.push(lastValue);
            }
            else
            {
                throw new IFML2ExpressionException("Системный сбой: в стеке значений осталось не одно (а {2}) значение!" +
                                                   "\nСтек операторов: {0}\nСтек значений: {1}", operatorStack, valueStack,
                        valueStack.size());
            }
        }

        @Override
        public String toString()
        {
            return "OperatorStack: " + operatorStack.toString() + " ; ValueStack: " + valueStack;
        }
    }
}
