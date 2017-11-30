package ifml2.editor;

import java.awt.*;

public class DataNotValidException extends IFML2EditorException
{
    private final Component componentForFocus;

    public DataNotValidException(String message, Component componentForFocus)
    {
        super(message);
        this.componentForFocus = componentForFocus;
    }

    public Component getComponentForFocus()
    {
        return componentForFocus;
    }
}
