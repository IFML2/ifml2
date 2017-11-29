package ifml2.vm;

import java.text.MessageFormat;

public class IFML2ExpressionException extends IFML2VMException
{
    public IFML2ExpressionException(String message, Object... arguments)
    {
        super(MessageFormat.format(message, arguments));
    }
}
