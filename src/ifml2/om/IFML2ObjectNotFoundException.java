package ifml2.om;

import ifml2.IFML2Exception;

public class IFML2ObjectNotFoundException extends IFML2Exception
{
    public IFML2ObjectNotFoundException(String message, Object... args)
    {
        super(message, args);
    }
}
