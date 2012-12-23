package ifml2.vm;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.vm.values.*;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Stack;

public class ExpressionCalculator
{
    private static final char GET_PROPERTY_OPERATOR = '.';
    private static final char COMPARE_OPERATOR = '=';
    private static final char ADD_OPERATOR = '+';
    private static final String NOT_OPERATOR = "не";
    private static final String AND_OPERATOR = "и";
    private static final String IN_OPERATOR = "в";

    private static final char QUOTE_CHAR = '"';
    private static final char SINGLE_QUOTE_CHAR = '\'';

    private RunningContext runningContext = null;

	private ExpressionCalculator(RunningContext runningContext)
	{
        this.runningContext = runningContext;
	}

    public static Value calculate(RunningContext runningContext, String expression) throws IFML2Exception
    {
        ExpressionCalculator expressionCalculator = new ExpressionCalculator(runningContext);
        return expressionCalculator.calculate(expression);
    }

    /**
     * What is just happened
     */
    enum ExprContextEnum
    {
        START,
        OPERATOR,
        OPERAND
    }

    Value calculate(String expression) throws IFML2Exception
	{
        String exp = expression.trim();

        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(exp));
        tokenizer.parseNumbers();
        tokenizer.commentChar(0);
        tokenizer.quoteChar(QUOTE_CHAR);
        tokenizer.quoteChar(SINGLE_QUOTE_CHAR);
        for(ExpressionOperatorEnum expressionOperator : ExpressionOperatorEnum.values())
        {
            if(expressionOperator.operatorCharacter != 0)
            {
                tokenizer.ordinaryChar(expressionOperator.operatorCharacter);
            }
        }

        CalculationStack calculationStack = new CalculationStack();

        int token;
        ExprContextEnum context = ExprContextEnum.START;
        try
        {
            while((token = tokenizer.nextToken()) != StreamTokenizer.TT_EOF)
            {
                switch (token)
                {
                    case StreamTokenizer.TT_WORD:
                        if(NOT_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            if(ExprContextEnum.OPERAND.equals(context))
                            {
                                // NOT can't follow operand!
                                throw new IFML2ExpressionException("Ошибка в выражении: НЕ не может следовать за операндом");
                            }
                            calculationStack.pushOperator(ExpressionOperatorEnum.NOT);
                            context = ExprContextEnum.OPERATOR;
                        }
                        else if(AND_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(ExpressionOperatorEnum.AND);
                                    context = ExprContextEnum.OPERATOR;
                                    break;
                                default:
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = ExprContextEnum.OPERAND;
                            }
                        }
                        else if(IN_OPERATOR.equalsIgnoreCase(tokenizer.sval))
                        {
                            switch (context)
                            {
                                case OPERAND:
                                    calculationStack.pushOperator(ExpressionOperatorEnum.IN);
                                    context = ExprContextEnum.OPERATOR;
                                    break;
                                default:
                                    calculationStack.pushSymbol(tokenizer.sval);
                                    context = ExprContextEnum.OPERAND;
                            }
                        }
                        else
                        {
                            if(ExprContextEnum.OPERAND.equals(context))
                            {
                                throw new IFML2ExpressionException("Ошибка в выражении: идентификатор ({0}) не может следовать за другим операндом", tokenizer.sval);
                            }
                            calculationStack.pushSymbol(tokenizer.sval);
                            context = ExprContextEnum.OPERAND;
                        }
                        break;
                    
                    case StreamTokenizer.TT_NUMBER:
                        if(ExprContextEnum.OPERAND.equals(context))
                        {
                            throw new IFML2ExpressionException("Ошибка в выражении: число ({0}) не может следовать за другим операндом", tokenizer.nval);
                        }
                        calculationStack.pushNumericLiteral(tokenizer.nval);
                        context = ExprContextEnum.OPERAND;
                        break;

                    case QUOTE_CHAR:
                    case SINGLE_QUOTE_CHAR:
                        if(ExprContextEnum.OPERAND.equals(context))
                        {
                            throw new IFML2ExpressionException("Ошибка в выражении: текст ({0}) не может следовать за другим операндом", tokenizer.sval);
                        }
                        calculationStack.pushTextLiteral(tokenizer.sval);
                        context = ExprContextEnum.OPERAND;
                        break;

                    case GET_PROPERTY_OPERATOR:
                        calculationStack.pushOperator(ExpressionOperatorEnum.GET_PROPERTY);
                        context = ExprContextEnum.OPERATOR;
                        break;

                    case COMPARE_OPERATOR:
                        calculationStack.pushOperator(ExpressionOperatorEnum.COMPARE);
                        context = ExprContextEnum.OPERATOR;
                        break;

                    case ADD_OPERATOR:
                        calculationStack.pushOperator(ExpressionOperatorEnum.ADD);
                        context = ExprContextEnum.OPERATOR;
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

        if(calculationStack.valueStack.size() == 0)
        {
            throw new IFML2VMException("Стек вычислений пуст!");
        }

        if(calculationStack.valueStack.size() > 1)
        {
            throw new IFML2VMException("В стеке вычислений больше одного значения ({0})!", calculationStack.valueStack);
        }

        return calculationStack.valueStack.pop();
    }

    private enum OperatorTypeEnum
    {
        BINARY,
        UNARY_RIGHT
    }

    private enum ExpressionOperatorEnum
    {
        GET_PROPERTY(GET_PROPERTY_OPERATOR, 100),
        IN(IN_OPERATOR, OperatorTypeEnum.BINARY, 50),
        NOT(NOT_OPERATOR, OperatorTypeEnum.UNARY_RIGHT, 30),
        ADD(ADD_OPERATOR, 20),
        COMPARE(COMPARE_OPERATOR, 10),
        AND(AND_OPERATOR, OperatorTypeEnum.BINARY, 5);

        public final char operatorCharacter;
        public String operatorString;
        public final int priority;
        private OperatorTypeEnum operatorType = OperatorTypeEnum.BINARY;

        ExpressionOperatorEnum(char operatorCharacter, int priority)
        {
            this.operatorCharacter = operatorCharacter;
            this.priority = priority;
        }

        ExpressionOperatorEnum(String operatorString, OperatorTypeEnum operatorType, int priority)
        {
            this((char) 0,  priority);
            this.operatorString = operatorString;
            this.operatorType = operatorType;
        }
    }

    private class CalculationStack
    {
        final Stack<Value> valueStack = new Stack<Value>();
        final Stack<ExpressionOperatorEnum> operatorStack = new Stack<ExpressionOperatorEnum>();

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

        public void pushOperator(ExpressionOperatorEnum operator) throws IFML2Exception
        {
            operatorStack.push(operator);
            shrink();
        }

        private void shrink() throws IFML2Exception
        {
            while(operatorStack.size() >= 2)
            {
                ExpressionOperatorEnum lastOperator = operatorStack.pop();
                ExpressionOperatorEnum prevOperator = operatorStack.pop();

                if(firstHasLessOrEqPriority(lastOperator, prevOperator))
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

        private Value doOperation(ExpressionOperatorEnum operator) throws IFML2Exception
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
                    ObjectValue objectValue;

                    if(leftValue instanceof ObjectValue)
                    {
                        objectValue = (ObjectValue) leftValue;
                    }
                    else
                    {
                        if(leftValue instanceof UnresolvedSymbolValue)
                        {
                            Value resolvedValue = resolveSymbol((UnresolvedSymbolValue)leftValue);

                            if(resolvedValue instanceof ObjectValue)
                            {
                                objectValue = (ObjectValue) resolvedValue;
                            }
                            else
                            {
                                throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                            }
                        }
                        else
                        {
                            throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                        }
                    }

                    if(!(rightValue instanceof UnresolvedSymbolValue))
                    {
                        throw new IFML2ExpressionException("Величина не является именем свойства ({0})", rightValue);
                    }

                    IFMLObject object = objectValue.value;

                    if(object == null)
                    {
                        throw new IFML2ExpressionException("Объект {0} не задан (пуст)", leftValue);
                    }

                    String propertyName = ((UnresolvedSymbolValue)rightValue).value;

                    Value propertyValue = object.getMemberValue(propertyName, runningContext);

                    if(propertyValue == null)
                    {
                        throw new IFML2ExpressionException("Свойство {0} не задано у объекта {1}", rightValue, leftValue);
                    }

                    result = propertyValue;

                    break;
                }

                case COMPARE:
                {
                    Value preparedLeftValue = leftValue;
                    Value preparedRightValue = rightValue;

                    if(preparedLeftValue instanceof UnresolvedSymbolValue)
                    {
                        preparedLeftValue = resolveSymbol((UnresolvedSymbolValue) preparedLeftValue);
                    }

                    if(preparedRightValue instanceof UnresolvedSymbolValue)
                    {
                        preparedRightValue = resolveSymbol((UnresolvedSymbolValue) preparedRightValue);
                    }

                    assert preparedLeftValue != null;
                    if(!preparedLeftValue.getClass().equals(preparedRightValue.getClass()))
                    {
                        throw new IFML2ExpressionException("Сравниваемые величины разного типа ({0} и {1})", leftValue, rightValue);
                    }

                    boolean boolResult = preparedLeftValue.equals(preparedRightValue);

                    result = new BooleanValue(boolResult);

                    break;
                }

                case ADD:
                {
                    Value preparedLeftValue = leftValue;
                    Value preparedRightValue = rightValue;

                    if(preparedLeftValue instanceof UnresolvedSymbolValue)
                    {
                        preparedLeftValue = resolveSymbol((UnresolvedSymbolValue) preparedLeftValue);
                    }

                    if(preparedRightValue instanceof UnresolvedSymbolValue)
                    {
                        preparedRightValue = resolveSymbol((UnresolvedSymbolValue) preparedRightValue);
                    }

                    assert preparedLeftValue != null;

                    if(preparedLeftValue instanceof IAddableValue)
                    {
                        result = ((IAddableValue) preparedLeftValue).add(preparedRightValue);
                    }
                    else if(preparedRightValue instanceof TextValue)
                    {
                        result = new TextValue(preparedLeftValue.toString() + ((TextValue) preparedRightValue).getValue());
                    }
                    else
                    {
                        throw new IFML2ExpressionException("Не поддерживается операция \"{0}\" между типом \"{1}\" и \"{2}\"",
                                Value.OperationEnum.ADD, preparedLeftValue.getTypeName(), preparedLeftValue.getTypeName());
                    }

                    break;
                }

                case AND:
                {
                    if(!(leftValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", leftValue);
                    }
                    if(!(rightValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                    }

                    boolean leftBoolValue = ((BooleanValue) leftValue).value;
                    boolean rightBoolValue = ((BooleanValue) rightValue).value;

                    result = new BooleanValue(leftBoolValue && rightBoolValue);

                    break;
                }

                case NOT:
                {
                    if(!(rightValue instanceof BooleanValue))
                    {
                        throw new IFML2ExpressionException("Величина не является логической ({0})", rightValue);
                    }

                    boolean booleanValue = ((BooleanValue) rightValue).value;

                    result = new BooleanValue(!booleanValue);

                    break;
                }

                case IN:
                {
                    Value preparedLeftValue = leftValue;
                    Value preparedRightValue = rightValue;

                    if(preparedLeftValue instanceof UnresolvedSymbolValue)
                    {
                        preparedLeftValue = resolveSymbol((UnresolvedSymbolValue) preparedLeftValue);
                    }

                    if(preparedRightValue instanceof UnresolvedSymbolValue)
                    {
                        preparedRightValue = resolveSymbol((UnresolvedSymbolValue) preparedRightValue);
                    }

                    if(!(preparedLeftValue instanceof ObjectValue))
                    {
                        throw new IFML2ExpressionException("Величина не является объектом ({0})", leftValue);
                    }

                    if(!(preparedRightValue instanceof CollectionValue))
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

        private boolean firstHasLessOrEqPriority(ExpressionOperatorEnum firstOperator, ExpressionOperatorEnum secondOperator)
        {
            return firstOperator.priority <= secondOperator.priority;
        }

        public void solve() throws IFML2Exception
        {
            shrink();

            if(!operatorStack.isEmpty() && valueStack.size() < 1)
            {
                throw new IFML2ExpressionException("Системный сбой: стек операторов не пуст или недостаточно значений в стеке значений!" +
                        "\nСтек операторов: {0}\nСтек значений: {1}", operatorStack, valueStack);
            }

            while(!operatorStack.isEmpty())
            {
                ExpressionOperatorEnum operator = operatorStack.pop();

                Value operationResult = doOperation(operator);

                valueStack.push(operationResult);
            }

            if(valueStack.size() == 1)
            {
                Value lastValue = valueStack.pop();

                if(lastValue instanceof UnresolvedSymbolValue)
                {
                    lastValue = resolveSymbol((UnresolvedSymbolValue) lastValue);
                }
                valueStack.push(lastValue);
            }
            else
            {
                throw new IFML2ExpressionException("Системный сбой: в стеке значений осталось не одно значение!" +
                        "\nСтек операторов: {0}\nСтек значений: {1}", operatorStack, valueStack);
            }
        }

        @Override
        public String toString()
        {
            return "OperatorStack: " + operatorStack.toString() + " ; ValueStack: " + valueStack;
        }
    }

    private Value resolveSymbol(UnresolvedSymbolValue unresolvedSymbolValue) throws IFML2VMException
    {
        String symbol = unresolvedSymbolValue.getValue().trim();
        return runningContext.resolveSymbol(symbol);
    }
}
