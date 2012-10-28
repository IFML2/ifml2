package ifml2;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GUIUtils
{
    public static void packAndCenterWindow(Window window)
    {
        window.pack();

        // center form
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        // Determine the new location of the window
        int w = window.getSize().width;
        int h = window.getSize().height;
        int x = (dim.width-w) / 2;
        int y = (dim.height-h) / 2;
        // Move the window
        window.setLocation(x, y);
    }

    public static void showErrorMessage(Component parentComponent, Throwable exception)
    {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        JOptionPane.showMessageDialog(parentComponent, stringWriter.toString(), "Произошла ошибка!", JOptionPane.ERROR_MESSAGE);
    }
}
