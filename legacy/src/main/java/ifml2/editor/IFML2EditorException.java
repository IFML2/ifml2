package ifml2.editor;

import ifml2.IFML2Exception;

public class IFML2EditorException extends IFML2Exception {
    public IFML2EditorException(String message) {
        super(message);
    }

    public IFML2EditorException(String message, Object... arguments) {
        super(message, arguments);
    }
}
