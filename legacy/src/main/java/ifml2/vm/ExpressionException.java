package ifml2.vm;

import java.text.MessageFormat;

class ExpressionException extends VMException
{
	public ExpressionException(String message)
	{
		super(message);
	}

    public ExpressionException(String message, Object ... arguments)
    {
        super(MessageFormat.format(message, arguments));
    }
}
