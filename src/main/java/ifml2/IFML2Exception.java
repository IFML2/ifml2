package ifml2;

import java.text.MessageFormat;

public class IFML2Exception extends Exception
{
    public IFML2Exception(String message)
    {
        super(message);
    }

    protected IFML2Exception()
    {
        super();
    }

    protected IFML2Exception(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IFML2Exception(Throwable cause)
    {
        super("Произошла ошибка типа " + cause.getClass().toString(), cause);
    }

    public IFML2Exception(String message, Object... arguments)
    {
        super(MessageFormat.format(message, arguments));
    }

    public IFML2Exception(Throwable cause, String message, Object... arguments)
    {
        super(MessageFormat.format(message, arguments), cause);
    }
}
