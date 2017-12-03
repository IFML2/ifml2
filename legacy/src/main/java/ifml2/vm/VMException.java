package ifml2.vm;

import java.text.MessageFormat;

import ifml2.IFML2Exception;

@SuppressWarnings("serial")
public class VMException extends IFML2Exception {
    public VMException(String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments));
    }

    public VMException(Exception e, String pattern, Object... arguments) {
        super(MessageFormat.format(pattern, arguments), e);
    }

    public VMException(String message) {
        super(message);
    }

    VMException() {
        super();
    }

    public VMException(String message, Throwable cause) {
        super(message, cause);
    }

    public VMException(Throwable cause) {
        super(cause);
    }
}
