package ifml2.unittests;

import ifml2.vm.ExpressionCalculator;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.NumberValue;
import ifml2.vm.values.Value;
import junit.framework.TestCase;

public class ExpressionCalculatorTest extends TestCase
{
    // 1+1
    public void testCalculateOnePlusOne() throws Exception
    {
        Value result = ExpressionCalculator.calculate(null, "1 + 1");
        if(!(result instanceof NumberValue && ((NumberValue)result).getValue() == 2))
        {
            throw new Exception("1 + 1 failed");
        }
    }

    // не да
    public void testCalculateNotYes() throws Exception
    {
        Value result = ExpressionCalculator.calculate(null, "не да");
        if(!(result instanceof BooleanValue && !((BooleanValue)result).getValue()))
        {
            throw new Exception("не да failed");
        }
    }
}
