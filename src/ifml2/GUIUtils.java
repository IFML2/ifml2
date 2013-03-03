package ifml2;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GUIUtils
{
    public static void packAndCenterWindow(@NotNull Window window)
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

    public static void showErrorMessage(Component parentComponent, @NotNull Throwable exception)
    {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        JOptionPane.showMessageDialog(parentComponent, stringWriter.toString(), "Произошла ошибка!", JOptionPane.ERROR_MESSAGE);
    }

    private static final String IFML2_EDITOR_GUI_IMAGES = "/ifml2/editor/gui/images/";
    private static ImageIcon getEditorIcon(String fileName)
    {
        return new ImageIcon(GUIUtils.class.getResource(IFML2_EDITOR_GUI_IMAGES + fileName));
    }
    public static final Icon ADD_ELEMENT_ICON = getEditorIcon("Add24.gif");
    public static final Icon EDIT_ELEMENT_ICON = getEditorIcon("Edit24.gif");
    public static final Icon DEL_ELEMENT_ICON = getEditorIcon("Delete24.gif");
    public static final Icon NEW_ELEMENT_ICON = getEditorIcon("New24.gif");
    public static final Icon OPEN_ICON = getEditorIcon("Open24.gif");
    public static final Icon SAVE_ICON = getEditorIcon("Save24.gif");
    public static final Icon PREFERENCES_ICON = getEditorIcon("Preferences24.gif");
    public static final Icon PLAY_ICON = getEditorIcon("Play24.gif");
    public static final Icon MOVE_LEFT_ICON = getEditorIcon("Back24.gif");
    public static final Icon MOVE_RIGHT_ICON = getEditorIcon("Forward24.gif");
    public static final Icon UP_ICON = getEditorIcon("Up24.gif");
    public static final Icon DOWN_ICON = getEditorIcon("Down24.gif");
}
