package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.vm.values.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Stack;

import static ifml2.vm.ExpressionCalculator.Context.*;
import static java.lang.String.format;

public class ExpressionCalculator
{
    private static final char GET_PROPERTY_OPERATOR = '.';
    private static final char EQUALITY_OPERATOR = '=';
    private static final char GREATER_OPERATOR = '>';
    private static final char LESSER_OPERATOR = '<';
    private static final String NOT_EQUAL_OPERATOR = "<>";
    private static final char ADD_OPERATOR = '+';
    private static final char SUBTRACT_OPERATOR = '-';
    private static final String NOT_OPERATOR = "не";
    private static final String AND_OPERATOR = "и";
    private static final String OR_OPERATOR = "или";
    private static final String IN_OPERATOR = "в";

    private static final char QUOTE_CHAR = '"';

    private static final char SINGLE_QUOTE_CHAR = '\'';

    private SymbolResolver symbolResolver = null;

    private ExpressionCalculator(SymbolResolver symbolResolver)
    {
        this.symbolResolver = symbolResolver;
    }

    public static Value calculate(@NotNull SymbolResolver symbolResolver, String expression) throws IFML2Exception
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
        for (Operation operation : Operation.values())
        {
            if (operation.operatorCharacter != 0)
            {
                tokenizer.ordinaryChar(operation.operatorCharacter);
            }
        }

        CalculationStack calculationStack = new CalculationStack();

        int token;
        Context context = START;
        try
        {
            while ((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)
            {
                switch (token)
                {
                    case StreamTokenizer.TT_WORD:
                        if (NOT_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context) {
                                case OPERAND:
                                    // NOT can't follow operand!
                                    throw new IFML2ExpressionException("Ошибка в выражении: НЕ не может следовать за операндом");
                                default:
                                    calculationStack.pushOperator(Operation.NOT);
                                    context = OPERATOR;
                                    break;
                            }
                        }
                        else if (AND_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.AND);
                                    context = OPERATOR;
                                    break;
                                default: // consider it's just id
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        }
                        else if (OR_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.OR);
                                    context = OPERATOR;
                                    break;
                                default: // consider it's just id
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        }
                        else if (IN_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.IN);
                                    context = OPERATOR;
                                    break;
                                default: // consider it's just id
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        }
                        else
                        {
                            // consider it's just id
                            switch (context) {
                                case OPERAND:
                                    throw new IFML2ExpressionException(
                                            "Ошибка в выражении: идентификатор \"{0}\" не может следовать за другим операндом", tokenizer.sval);
                                default:
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                                    break;
                            }
                        }
                        break;

                    case StreamTokenizer.TT_NUMBER:
                        switch (context) {
                            case OPERAND:
                                throw new IFML2ExpressionException("Ошибка в выражении: число \"{0}\" не может следовать за другим операндом",
                                        tokenizer.nval);
                            default:
                                calculationStack.pushNumericLiteral(tokenizer.nval);
                                context = OPERAND;
                                break;
                        }
                        break;

                    case QUOTE_CHAR:
                    case SINGLE_QUOTE_CHAR:
                        switch (context) {
                            case OPERAND:
                                throw new IFML2ExpressionException("Ошибка в выражении: текст \"{0}\" не может следовать за другим операндом",
                                        tokenizer.sval);
                            default:
                                calculationStack.pushTextLiteral(tokenizer.sval);
                                context = OPERAND;
                                break;
                        }
                        break;

                    case GET_PROPERTY_OPERATOR:
                        calculationStack.pushOperator(Operation.GET_PROPERTY);
                        context = OPERATOR;
                        break;

                    case EQUALITY_OPERATOR:
                        calculationStack.pushOperator(Operation.COMPARE_EQUALITY);
                        context = OPERATOR;
                        break;

                    case GREATER_OPERATOR:
                        calculationStack.pushOperator(Operation.COMPARE_GREATER);
                        context = OPERATOR;
                        break;

                    case LESSER_OPERATOR:
                        // check it's not not equal <>
                        int nextToken = tokenizer.nextToken();
                        if (nextToken == GREATER_OPERATOR) // it's <> !
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.COMPARE_NOT_EQUALITY);
                                    context = OPERATOR;
                                    break;
                                default:
                                    throw new IFML2ExpressionException("Ошибка в выражении: {0} может следовать только за операндом",
                                            NOT_EQUAL_OPERATOR);
                            }
                        }
                        else {
                            tokenizer.pushBack(); // it's not <> so return consumed token and process it next
                            calculationStack.pushOperator(Operation.COMPARE_LESSER);
                            context = OPERATOR;
                        }
                        break;

                    case ADD_OPERATOR:
                        calculationStack.pushOperator(Operation.ADD);
                        context = OPERATOR;
                        break;

                    case SUBTRACT_OPERATOR:
                        // decide if it subtract or negative number
                        switch (context)
                        {
                            case START:
                            case OPERATOR:
                                // it can be only negative number at start ow after operator (todo consider unary minus)
                                nextToken = tokenizer.nextToken(); // check that next token is number
                                if (nextToken == StreamTokenizer.TT_NUMBER)
                                {
                                    // consume it both as negative number
                                    calculationStack.pushNumericLiteral(-tokenizer.nval);
                                    context = OPERAND;
                                }
                                else
                                {
                                    tokenizer.pushBack(); // return token back
                                    // fail
                                    throw new IFML2ExpressionException("Ошибка в выражении: унарный минус \"{0}\" может быть только перед числом",
                                            SUBTRACT_OPERATOR);
                                }
                                break;
                            case OPERAND:
                                // it can only be subtraction
                                calculationStack.pushOperator(Operation.SUBTRACT);
                                context = OPERATOR;
                                break;
                        }
                        break;

                    default:
                        throw new IFML2ExpressionException("Ошибка в выражении \"{0}\" - неизвестный токен \"{1}\"", expression, token);
                }
            }
        }
        catch (IOException e)
        {
            throw new IFML2Exception(e);
        }

        return calculationStack.solve();
    }

    /**
     * What is just happened
     */
    enum Context
    {
        START,
        OPERATOR,
        OPERAND
    }
    /*
    TODO: ??? 27.02.2016 переименовать в BINARY_OPERATOR, добавить UNARY_RIGHT_OPERATOR,
    запретить после UNARY_RIGHT любой оператор ??? (он ждёт операнд справа), а после BINARY запретить только BINARY (следующий ждёт слева операнд...)
    */

    private enum OperationType
    {
        UNARY_RIGHT,
        BINARY
    }

    private enum Operation
    {
        GET_PROPERTY(GET_PROPERTY_OPERATOR, 100),
        IN(IN_OPERATOR, OperationType.BINARY, 50),
        NOT(NOT_OPERATOR, OperationType.UNARY_RIGHT, 40),
        ADD(ADD_OPERATOR, 30),
        SUBTRACT(SUBTRACT_OPERATOR, 30),
        COMPARE_EQUALITY(EQUALITY_OPERATOR, 20),
        COMPARE_NOT_EQUALITY(NOT_EQUAL_OPERATOR, OperationType.BINARY, 20),
        COMPARE_GREATER(GREATER_OPERATOR, 20),
        COMPARE_LESSER(LESSER_OPERATOR, 20),
        AND(AND_OPERATOR, OperationType.BINARY, 10),
        OR(OR_OPERATOR, OperationType.BINARY, 5);

        public final char operatorCharacter;
        public final int priority;
        public String operatorString;
        private OperationType operationType = OperationType.BINARY;

        Operation(char operatorCharacter, int priority)
        {
            this.operatorCharacter = operatorCharacter;
            this.priority = priority;
        }

        Operation(String operatorString, OperationType operationType, int priority)
        {
            this((char) 0, priority);
            this.operatorString = operatorString;
            this.operationType = operationType;
        }

        @Contract(pure = true)
        public boolean canSqueeze(Operation operator) {
            return priority <= operator.priority && OperationType.UNARY_RIGHT != operator.getOperationType();
        }

        public OperationType getOperationType() {
            return operationType;
        }
    }

    private class CalculationStack
    {
        private final Stack<Operation> operatorStack = new Stack<>();
        private final Stack<Expression> expressionStack = new Stack<>();

        public void pushSymbol(String symbol)
        {
            expressionStack.push(new ValueExpression(new SymbolValue(symbol)));
        }

        public void pushTextLiteral(String text)
        {
            expressionStack.push(new ValueExpression(new TextValue(text)));
        }

        public void pushNumericLiteral(double number)
        {
            expressionStack.push(new ValueExpression(new NumberValue(number)));
        }

        public void pushOperator(Operation operator) throws IFML2Exception
        {
            operatorStack.push(operator);
            shrink();
        }

        private void shrink() throws IFML2Exception
        {
            while (operatorStack.size() >= 2)
            {
                Operation lastOperator = operatorStack.pop();
                Operation prevOperator = operatorStack.pop();

                if (lastOperator.canSqueeze(prevOperator))
                {
                    Expression expression = doExpression(prevOperator);
                    expressionStack.push(expression);
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

        /*private Value doOperation(Operation operator) throws IFML2Exception
        {
            switch (operator)
            {
                case GET_PROPERTY:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    if (!(resolvedLeftValue instanceof ObjectValue))
                    {
                        throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                    }

                    if (!(rightValue instanceof SymbolValue))
                    {
                        throw new IFML2ExpressionException("Величина не является именем свойства ({0})", rightValue);
                    }

                    IFMLObject object = ((ObjectValue) resolvedLeftValue).getValue();
                    if (object == null)
                    {
                        throw new IFML2ExpressionException("Объект {0} не задан (пуст)", leftValue);
                    }

                    String propertyName = ((SymbolValue) rightValue).getValue();
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

                case COMPARE_NOT_EQUALITY:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    assert resolvedLeftValue != null;

                    Value.CompareResult compareResult = resolvedLeftValue.compareTo(resolvedRightValue);
                    switch (compareResult)
                    {
                        case EQUAL:
                            result = new BooleanValue(false);
                            break;
                        case UNEQUAL:
                        case LEFT_BIGGER:
                        case RIGHT_BIGGER:
                            result = new BooleanValue(true);
                            break;
                        case NOT_APPLICABLE:
                            throw new IFML2ExpressionException("Сравниваемые величины разного типа ({0} и {1})", leftValue, rightValue);

                        default:
                            throw new IFML2VMException("Неизвестный результат сравнения величин ({0})", compareResult);
                    }

                    break;
                }

                case COMPARE_GREATER:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    assert resolvedLeftValue != null;

                    if (!(resolvedLeftValue instanceof NumberValue))
                    {
                        throw new IFML2VMException("Левая величина сравнения должна быть числом (а её тип {0}).",
                                resolvedLeftValue.getTypeName());
                    }

                    if (!(resolvedRightValue instanceof NumberValue))
                    {
                        throw new IFML2VMException("Правая величина сравнения должна быть числом (а её тип {0}).",
                                resolvedRightValue.getTypeName());
                    }

                    NumberValue leftNumber = (NumberValue) resolvedLeftValue;
                    NumberValue rightNumber = (NumberValue) resolvedRightValue;
                    result = new BooleanValue(leftNumber.getValue() > rightNumber.getValue());

                    break;
                }

                case COMPARE_LESSER:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    assert resolvedLeftValue != null;

                    if (!(resolvedLeftValue instanceof NumberValue))
                    {
                        throw new IFML2VMException("Левая величина сравнения должна быть числом (а её тип {0}).",
                                resolvedLeftValue.getTypeName());
                    }

                    if (!(resolvedRightValue instanceof NumberValue))
                    {
                        throw new IFML2VMException("Правая величина сравнения должна быть числом (а её тип {0}).",
                                resolvedRightValue.getTypeName());
                    }

                    NumberValue leftNumber = (NumberValue) resolvedLeftValue;
                    NumberValue rightNumber = (NumberValue) resolvedRightValue;
                    result = new BooleanValue(leftNumber.getValue() < rightNumber.getValue());

                    break;
                }

                case SUBTRACT:
                {
                    Value resolvedLeftValue = ensureValueResolved(leftValue);
                    Value resolvedRightValue = ensureValueResolved(rightValue);

                    assert resolvedLeftValue != null;

                    if (!(resolvedLeftValue instanceof NumberValue))
                    {
                        throw new IFML2VMException("Левая величина сравнения должна быть числом (а её тип {0}).",
                                resolvedLeftValue.getTypeName());
                    }

                    if (!(resolvedRightValue instanceof NumberValue))
                    {
                        throw new IFML2VMException("Правая величина сравнения должна быть числом (а её тип {0}).",
                                resolvedRightValue.getTypeName());
                    }

                    result = new NumberValue(((NumberValue) resolvedLeftValue).getValue() - ((NumberValue) resolvedRightValue).getValue());

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
        }*/

        public Value solve() throws IFML2Exception
        {
            shrink();

            if (!operatorStack.isEmpty() && expressionStack.isEmpty())
            {
                throw new IFML2ExpressionException("Системный сбой: стек операторов не пуст, при этом нет значений в стеке выражений!" +
                        "\nСтек операторов: {0}\nСтек выражений: {1}", operatorStack, expressionStack);
            }

            while (!operatorStack.isEmpty())
            {
                Operation operator = operatorStack.pop();
                Expression expression = doExpression(operator);
                expressionStack.push(expression);
            }

            if (expressionStack.size() == 1)
            {
                Expression lastExpression = expressionStack.pop();
                return lastExpression.solve();
            }
            else
            {
                throw new IFML2ExpressionException("Системный сбой: в стеке выражений осталось не одно (а {2}) значение!" +
                        "\nСтек операторов: {0}\nСтек выражений: {1}", operatorStack, expressionStack,
                        expressionStack.size());
            }
        }

        private Expression doExpression(Operation operator) throws IFML2ExpressionException {
            Expression rightExpr;
            Expression leftExpr = null;

            switch (operator.operationType)
            {
                case BINARY:
                    rightExpr = expressionStack.pop();
                    leftExpr = expressionStack.pop();
                    break;

                case UNARY_RIGHT:
                    rightExpr = expressionStack.pop();
                    break;

                default:
                    throw new IFML2ExpressionException("Неизвестный тип операции {0}", operator.operationType);
            }

            switch (operator) {
                case ADD:
                    assert leftExpr != null;
                    return new AddExpression(leftExpr, rightExpr);
                case AND:
                    assert leftExpr != null;
                    return new AndExpression(leftExpr, rightExpr);
                case OR:
                    assert leftExpr != null;
                    return new OrExpression(leftExpr, rightExpr);
                case NOT:
                    return new NotExpression(rightExpr);
                case GET_PROPERTY: // TODO: 26.02.2016
                    return new GetPropertyExpression(leftExpr, rightExpr);
                case IN: // TODO: 26.02.2016
                case COMPARE_EQUALITY: // TODO: 26.02.2016
                case COMPARE_NOT_EQUALITY: // TODO: 26.02.2016
                case COMPARE_GREATER: // TODO: 26.02.2016
                case COMPARE_LESSER: // TODO: 26.02.2016
                default:
                    throw new IFML2ExpressionException("Операция {0} пока не поддерживается в выражениях", operator.toString());
            }
        }

        @Override
        public String toString() {
            return format("OperatorStack: %s; ExpressionStack: %s", operatorStack, expressionStack);
        }

        private abstract class Expression {
            public abstract Value solve() throws IFML2VMException;

            protected abstract String toStringRepresentation();

            @Override
            public String toString() {
                return toStringRepresentation();
            }
        }

        private final class ValueExpression extends Expression {
            private final Value value;

            public ValueExpression(@NotNull Value value) {
                super();
                this.value = value;
            }

            @Override
            public Value solve() throws IFML2VMException {
                if (value instanceof SymbolValue) {
                    return ((SymbolValue) value).resolve(symbolResolver);
                }
                return value;
            }

            @Override
            protected String toStringRepresentation() {
                return format("Value[%s]", value.toLiteral());
            }

            public Value getValue() {
                return value;
            }
        }

        private abstract class UnaryExpression extends Expression {
            protected final Expression operandExpr;

            protected UnaryExpression(Expression operandExpr) {
                super();
                this.operandExpr = operandExpr;
            }
        }

        private abstract class BinaryExpression extends Expression {
            protected final Expression leftExpr;
            protected final Expression rightExpr;

            public BinaryExpression(@NotNull Expression leftExpr, @NotNull Expression rightExpr) {
                super();
                this.leftExpr = leftExpr;
                this.rightExpr = rightExpr;
            }
        }

        private final class AddExpression extends BinaryExpression {
            public AddExpression(@NotNull Expression leftExpr, @NotNull Expression rightExpr) {
                super(leftExpr, rightExpr);
            }

            @Override
            protected String toStringRepresentation() {
                return format("Add[%s,%s]", leftExpr, rightExpr);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                Value rightValue = rightExpr.solve();

                if (leftValue instanceof IAddableValue)
                {
                    return ((IAddableValue) leftValue).add(rightValue);
                }
                else if (rightValue instanceof TextValue)
                {
                    // если левый операнд не поддерживает сложение, но правый - текст, то всё превращаем в строку и клеим
                    return new TextValue(leftValue.toString() + ((TextValue) rightValue).getValue());
                }
                else
                {
                    throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"",
                            Value.Operation.ADD, leftValue.getTypeName(), leftValue.getTypeName());
                }
            }
        }

        private final class AndExpression extends BinaryExpression {
            public AndExpression(@NotNull Expression leftExpr, @NotNull Expression rightExpr) {
                super(leftExpr, rightExpr);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                if (!(leftValue instanceof BooleanValue)) {
                    throw new IFML2ExpressionException("Величина не является логической ({0})", leftValue);
                }

                // incomplete boolean evaluation!
                Boolean leftBoolValue = ((BooleanValue) leftValue).getValue();
                if (!leftBoolValue) {
                    return new BooleanValue(false);
                }

                Value rightValue = rightExpr.solve();
                if (!(rightValue instanceof BooleanValue)) {
                    throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                }

                Boolean rightBoolValue = ((BooleanValue) rightValue).getValue();

                return new BooleanValue(rightBoolValue); // depends on right cause of incomplete evaluation
            }

            @Override
            protected String toStringRepresentation() {
                return format("And[%s,%s]", leftExpr, rightExpr);
            }
        }

        private final class OrExpression extends BinaryExpression {
            public OrExpression(@NotNull Expression leftExpr, @NotNull Expression rightExpr) {
                super(leftExpr, rightExpr);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                if (!(leftValue instanceof BooleanValue)) {
                    throw new IFML2ExpressionException("Величина не является логической ({0})", leftValue);
                }

                // incomplete boolean evaluation!
                Boolean leftBoolValue = ((BooleanValue) leftValue).getValue();
                if (leftBoolValue) {
                    return new BooleanValue(true);
                }

                Value rightValue = rightExpr.solve();
                if (!(rightValue instanceof BooleanValue)) {
                    throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                }

                Boolean rightBoolValue = ((BooleanValue) rightValue).getValue();

                return new BooleanValue(rightBoolValue); // depends on right cause of incomplete evaluation
            }

            @Override
            protected String toStringRepresentation() {
                return format("Or[%s,%s]", leftExpr, rightExpr);
            }
        }

        private class NotExpression extends UnaryExpression {
            public NotExpression(Expression expression) {
                super(expression);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value value = operandExpr.solve();
                if (!(value instanceof BooleanValue)) {
                    throw new IFML2ExpressionException("Величина не является логической ({0})", value);
                }

                boolean boolValue = ((BooleanValue) value).getValue();

                return new BooleanValue(!boolValue);
            }

            @Override
            protected String toStringRepresentation() {
                return format("Not[%s]", operandExpr);
            }
        }

        private class GetPropertyExpression extends BinaryExpression {
            public GetPropertyExpression(Expression leftExpr, Expression rightExpr) {
                super(leftExpr, rightExpr);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                if (!(leftValue instanceof ObjectValue)) {
                    throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                }

                IFMLObject object = ((ObjectValue) leftValue).getValue();
                if (object == null) {
                    throw new IFML2ExpressionException("Объект {0} не задан (пуст)", leftValue);
                }

                if (!(rightExpr instanceof ValueExpression)) {
                    throw new IFML2ExpressionException("Выражение не является именем свойства ({0})", rightExpr);
                }

                ValueExpression valueExpression = (ValueExpression) rightExpr;
                Value value = valueExpression.getValue();
                if (!(value instanceof SymbolValue)) {
                    throw new IFML2ExpressionException("Величина не является именем свойства ({0})", value);
                }

                String propertyName = ((SymbolValue) value).getValue();
                Value propertyValue;
                try {
                    propertyValue = object.getMemberValue(propertyName, symbolResolver);
                } catch (IFML2Exception e) {
                    throw new IFML2VMException(e, "Ошибка во время получения свойства {0} у объекта {1}", propertyName, object);
                }
                return propertyValue == null ? new EmptyValue() : propertyValue;
            }

            @Override
            protected String toStringRepresentation() {
                return format("GetProperty[%s,%s]", leftExpr, rightExpr);
            }
        }
    }
}
