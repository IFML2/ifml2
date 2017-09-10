package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.EmptyValue;
import ifml2.vm.values.IAddableValue;
import ifml2.vm.values.NumberValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.SymbolValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Collection;
import java.util.Stack;

import static ifml2.vm.ExpressionCalculator.Context.OPERAND;
import static ifml2.vm.ExpressionCalculator.Context.OPERATOR;
import static ifml2.vm.ExpressionCalculator.Context.START;
import static java.lang.String.format;

public class ExpressionCalculator {
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

    private ExpressionCalculator(SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
    }

    public static Value calculate(@NotNull SymbolResolver symbolResolver, String expression) throws IFML2Exception {
        ExpressionCalculator expressionCalculator = new ExpressionCalculator(symbolResolver);
        return expressionCalculator.calculate(expression);
    }

    Value calculate(String expression) throws IFML2Exception {
        String exp = expression.trim();

        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(exp));
        tokenizer.parseNumbers();
        tokenizer.commentChar(0);
        tokenizer.quoteChar(QUOTE_CHAR);
        tokenizer.quoteChar(SINGLE_QUOTE_CHAR);
        for (Operation operation : Operation.values()) {
            if (operation.operatorCharacter != 0) {
                tokenizer.ordinaryChar(operation.operatorCharacter);
            }
        }

        CalculationStack calculationStack = new CalculationStack();

        int token;
        Context context = START;
        try {
            while ((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        if (NOT_OPERATOR.equalsIgnoreCase(tokenizer.sval)) {
                            switch (context) {
                                case OPERAND:
                                    // NOT can't follow operand!
                                    throw new IFML2ExpressionException("Ошибка в выражении: НЕ не может следовать за операндом");
                                default:
                                    calculationStack.pushOperator(Operation.NOT);
                                    context = OPERATOR;
                                    break;
                            }
                        } else if (AND_OPERATOR.equalsIgnoreCase(tokenizer.sval)) {
                            switch (context) {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.AND);
                                    context = OPERATOR;
                                    break;
                                default: // consider it's just id
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        } else if (OR_OPERATOR.equalsIgnoreCase(tokenizer.sval)) {
                            switch (context) {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.OR);
                                    context = OPERATOR;
                                    break;
                                default: // consider it's just id
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        } else if (IN_OPERATOR.equalsIgnoreCase(tokenizer.sval)) {
                            switch (context) {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.IN);
                                    context = OPERATOR;
                                    break;
                                default: // consider it's just id
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = OPERAND;
                            }
                        } else {
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
                            switch (context) {
                                case OPERAND:
                                    calculationStack.pushOperator(Operation.COMPARE_NOT_EQUALITY);
                                    context = OPERATOR;
                                    break;
                                default:
                                    throw new IFML2ExpressionException("Ошибка в выражении: {0} может следовать только за операндом",
                                            NOT_EQUAL_OPERATOR);
                            }
                        } else {
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
                        switch (context) {
                            case START:
                            case OPERATOR:
                                // it can be only negative number at start ow after operator (todo consider unary minus)
                                nextToken = tokenizer.nextToken(); // check that next token is number
                                if (nextToken == StreamTokenizer.TT_NUMBER) {
                                    // consume it both as negative number
                                    calculationStack.pushNumericLiteral(-tokenizer.nval);
                                    context = OPERAND;
                                } else {
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
        } catch (IOException e) {
            throw new IFML2Exception(e);
        }

        return calculationStack.solve();
    }

    /**
     * What is just happened
     */
    enum Context {
        START,
        OPERATOR,
        OPERAND
    }
    /*
    TODO: ??? 27.02.2016 переименовать в BINARY_OPERATOR, добавить UNARY_RIGHT_OPERATOR,
    запретить после UNARY_RIGHT любой оператор ??? (он ждёт операнд справа), а после BINARY запретить только BINARY (следующий ждёт слева операнд...)
    */

    private enum OperationType {
        UNARY_RIGHT,
        BINARY
    }

    private enum Operation {
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
        private OperationType type = OperationType.BINARY;

        Operation(char operatorCharacter, int priority) {
            this.operatorCharacter = operatorCharacter;
            this.priority = priority;
        }

        Operation(String operatorString, OperationType type, int priority) {
            this((char) 0, priority);
            this.operatorString = operatorString;
            this.type = type;
        }

        @Contract(pure = true)
        public boolean canSqueeze(Operation operator) {
            return priority <= operator.priority && type != OperationType.UNARY_RIGHT;
        }

        public OperationType getType() {
            return type;
        }
    }

    private class CalculationStack {
        private final Stack<Operation> operatorStack = new Stack<>();
        private final Stack<Expression> expressionStack = new Stack<>();

        public void pushSymbol(String symbol) {
            expressionStack.push(new ValueExpression(new SymbolValue(symbol), symbolResolver));
        }

        public void pushTextLiteral(String text) {
            expressionStack.push(new ValueExpression(new TextValue(text), symbolResolver));
        }

        public void pushNumericLiteral(double number) {
            expressionStack.push(new ValueExpression(new NumberValue(number), symbolResolver));
        }

        public void pushOperator(Operation operator) throws IFML2Exception {
            operatorStack.push(operator);
            shrink();
        }

        private void shrink() throws IFML2Exception {
            while (operatorStack.size() >= 2) {
                Operation lastOperator = operatorStack.pop();
                Operation prevOperator = operatorStack.pop();

                if (lastOperator.canSqueeze(prevOperator)) {
                    Expression expression = doExpression(prevOperator);
                    expressionStack.push(expression);
                    operatorStack.push(lastOperator);
                } else {
                    operatorStack.push(prevOperator);
                    operatorStack.push(lastOperator);

                    break;
                }
            }
        }

        public Value solve() throws IFML2Exception {
            shrink();

            if (!operatorStack.isEmpty() && expressionStack.isEmpty()) {
                throw new IFML2ExpressionException("Системный сбой: стек операторов не пуст, при этом нет значений в стеке выражений!" +
                        "\nСтек операторов: {0}\nСтек выражений: {1}", operatorStack, expressionStack);
            }

            while (!operatorStack.isEmpty()) {
                Operation operator = operatorStack.pop();
                Expression expression = doExpression(operator);
                expressionStack.push(expression);
            }

            if (expressionStack.size() == 1) {
                Expression lastExpression = expressionStack.pop();
                final Value solutionResult = lastExpression.solve();
                return solutionResult;
            } else {
                throw new IFML2ExpressionException("Системный сбой: в стеке выражений осталось не одно (а {2}) значение!" +
                        "\nСтек операторов: {0}\nСтек выражений: {1}", operatorStack, expressionStack,
                        expressionStack.size());
            }
        }

        private Expression doExpression(Operation operation) throws IFML2ExpressionException {
            Expression rightExpr;
            Expression leftExpr = null;

            switch (operation.type) {
                case BINARY:
                    rightExpr = expressionStack.pop();
                    leftExpr = expressionStack.pop();
                    break;

                case UNARY_RIGHT:
                    rightExpr = expressionStack.pop();
                    break;

                default:
                    throw new IFML2ExpressionException("Неизвестный тип операции {0}", operation.getType());
            }

            switch (operation) {
                case ADD:
                    return new AddExpression(leftExpr, rightExpr);
                case AND:
                    return new AndExpression(leftExpr, rightExpr);
                case OR:
                    return new OrExpression(leftExpr, rightExpr);
                case NOT:
                    return new NotExpression(rightExpr);
                case GET_PROPERTY:
                    return new GetPropertyExpression(leftExpr, rightExpr, symbolResolver);
                case IN:
                    return new InExpression(leftExpr, rightExpr);
                case COMPARE_EQUALITY:
                    return new EqualsExpression(leftExpr, rightExpr, true);
                case COMPARE_NOT_EQUALITY:
                    return new EqualsExpression(leftExpr, rightExpr, false);
                case COMPARE_GREATER:
                    return new CompareGreaterExpression(leftExpr, rightExpr, true);
                case COMPARE_LESSER:
                    return new CompareGreaterExpression(leftExpr, rightExpr, false);
                case SUBTRACT:
                    return new SubtractExpression(leftExpr, rightExpr);
                default:
                    throw new IFML2ExpressionException("Операция {0} временно поддерживается в выражениях", operation.toString());
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
            private SymbolResolver symbolResolver;

            public ValueExpression(@NotNull Value value, SymbolResolver symbolResolver) {
                super();
                this.value = value;
                this.symbolResolver = symbolResolver;
            }

            @Override
            public Value solve() throws IFML2VMException {
                if (value instanceof SymbolValue) {
                    return ((SymbolValue) value).resolve(this.symbolResolver);
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

            public BinaryExpression(Expression leftExpr, @NotNull Expression rightExpr) throws IFML2ExpressionException {
                super();
                if (leftExpr == null) {
                    throw new IFML2ExpressionException(format("Нет левого операнда для операции \"%s\"", this));
                }
                this.leftExpr = leftExpr;
                this.rightExpr = rightExpr;
            }
        }

        private final class AddExpression extends BinaryExpression {
            public AddExpression(Expression leftExpr, @NotNull Expression rightExpr) throws IFML2ExpressionException {
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

                if (leftValue instanceof IAddableValue) {
                    return ((IAddableValue) leftValue).add(rightValue);
                } else if (rightValue instanceof TextValue) {
                    // если левый операнд не поддерживает сложение, но правый - текст, то всё превращаем в строку и клеим
                    return new TextValue(leftValue.toString() + ((TextValue) rightValue).getValue());
                } else {
                    throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"",
                            Value.Operation.ADD, leftValue.getTypeName(), leftValue.getTypeName());
                }
            }
        }

        private final class AndExpression extends BinaryExpression {
            public AndExpression(Expression leftExpr, @NotNull Expression rightExpr) throws IFML2ExpressionException {
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
            public OrExpression(Expression leftExpr, @NotNull Expression rightExpr) throws IFML2ExpressionException {
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
            public NotExpression(@NotNull Expression expression) {
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
            private SymbolResolver symbolResolver;

            public GetPropertyExpression(Expression leftExpr, @NotNull Expression rightExpr, SymbolResolver symbolResolver) throws IFML2ExpressionException {
                super(leftExpr, rightExpr);
                this.symbolResolver = symbolResolver;
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                if (!(leftValue instanceof ObjectValue)) {
                    throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                }

                IFMLObject ifmlObject = ((ObjectValue) leftValue).getValue();
                if (ifmlObject == null) {
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
                    propertyValue = ifmlObject.getMemberValue(propertyName, this.symbolResolver);
                } catch (IFML2Exception e) {
                    throw new IFML2VMException(e, "Ошибка во время получения свойства {0} у объекта {1}", propertyName, ifmlObject);
                }
                return propertyValue == null ? new EmptyValue() : propertyValue;
            }

            @Override
            protected String toStringRepresentation() {
                return format("GetProperty[%s,%s]", leftExpr, rightExpr);
            }
        }

        private class InExpression extends BinaryExpression {
            public InExpression(Expression leftExpr, Expression rightExpr) throws IFML2ExpressionException {
                super(leftExpr, rightExpr);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                if (!(leftValue instanceof ObjectValue)) {
                    throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                }

                Value rightValue = rightExpr.solve();
                if (!(rightValue instanceof CollectionValue)) {
                    throw new IFML2ExpressionException("Величина не является коллекцией ({0})", rightValue);
                }

                IFMLObject leftObjectValue = ((ObjectValue) leftValue).getValue();
                Collection rightCollectionValue = ((CollectionValue) rightValue).getValue();

                return new BooleanValue(rightCollectionValue.contains(leftObjectValue));
            }

            @Override
            protected String toStringRepresentation() {
                return format("In[%s,%s]", leftExpr, rightExpr);
            }
        }

        private class EqualsExpression extends BinaryExpression {
            private final boolean isPositive;

            public EqualsExpression(Expression leftExpr, Expression rightExpr, boolean isPositive) throws IFML2ExpressionException {
                super(leftExpr, rightExpr);
                this.isPositive = isPositive;
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                Value rightValue = rightExpr.solve();

                Value.CompareResult compareResult = leftValue.compareTo(rightValue);
                switch (compareResult) {
                    case EQUAL:
                        return new BooleanValue(isPositive);
                    case UNEQUAL:
                    case LEFT_BIGGER:
                    case RIGHT_BIGGER:
                        return new BooleanValue(!isPositive);
                    case NOT_APPLICABLE:
                        throw new IFML2ExpressionException("Сравниваемые величины разного типа ({0} и {1})", leftValue, rightValue);

                    default:
                        throw new IFML2VMException("Неизвестный результат сравнения величин ({0})", compareResult);
                }
            }

            @Override
            protected String toStringRepresentation() {
                final String operationName = isPositive ? "Equ" : "NotEqu";
                return format("%s[%s,%s]", operationName, leftExpr, rightExpr);
            }
        }

        private class CompareGreaterExpression extends BinaryExpression {
            private final boolean isPositive;

            public CompareGreaterExpression(Expression leftExpr, Expression rightExpr, boolean isPositive) throws IFML2ExpressionException {
                super(leftExpr, rightExpr);
                this.isPositive = isPositive;
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();

                Value rightValue = rightExpr.solve();

                Value.CompareResult compareResult = leftValue.compareTo(rightValue);
                switch (compareResult) {
                    case LEFT_BIGGER:
                        return new BooleanValue(isPositive);
                    case RIGHT_BIGGER:
                        return new BooleanValue(!isPositive);
                    case EQUAL:
                        return new BooleanValue(false);
                    default:
                        throw new IFML2ExpressionException(format("Непринменимая операция \">\" для операндов типов %s и %s",
                                leftValue.getTypeName(), rightValue.getTypeName()));
                }
            }

            @Override
            protected String toStringRepresentation() {
                final String operationName = isPositive ? "Greater" : "Lesser";
                return format("%s[%s,%s]", operationName, leftExpr, rightExpr);
            }
        }

        private class SubtractExpression extends BinaryExpression {
            public SubtractExpression(Expression leftExpr, Expression rightExpr) throws IFML2ExpressionException {
                super(leftExpr, rightExpr);
            }

            @Override
            public Value solve() throws IFML2VMException {
                Value leftValue = leftExpr.solve();
                if (!(leftValue instanceof NumberValue)) {
                    throw new IFML2VMException("Левая величина сравнения должна быть числом (а её тип {0}).",
                            leftValue.getTypeName());
                }

                Value rightValue = rightExpr.solve();
                if (!(rightValue instanceof NumberValue)) {
                    throw new IFML2VMException("Правая величина сравнения должна быть числом (а её тип {0}).",
                            rightValue.getTypeName());
                }

                return new NumberValue(((NumberValue) leftValue).getValue() - ((NumberValue) rightValue).getValue());
            }

            @Override
            protected String toStringRepresentation() {
                return format("Subtract[%s,%s]", leftExpr, rightExpr);
            }
        }
    }
}
