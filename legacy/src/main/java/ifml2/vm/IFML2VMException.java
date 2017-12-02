package ifml2.vm;

import java.text.MessageFormat;

import ifml2.IFML2Exception;

@SuppressWarnings("serial")
public class IFML2VMException extends IFML2Exception {
    public IFML2VMException(String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments));
    }

    public IFML2VMException(Exception e, String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments), e);
    }

    public IFML2VMException(String message) {
        super(message);
    }

    public IFML2VMException(String message, Throwable cause) {
        super(message, cause);
    }

    public IFML2VMException(Throwable cause) {
        super(cause);
    }
}
