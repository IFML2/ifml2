package ifml2.configuration;

import ifml2.IFML2Exception;

public class IFML2ConfigurationException extends IFML2Exception {
    public IFML2ConfigurationException(Throwable cause, String message) {
        super(message, cause);
    }

    public IFML2ConfigurationException(String message) {
        super(message);
    }
}
