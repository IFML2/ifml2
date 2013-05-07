package ifml2;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

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

    /**
     * Shows delete confirmation dialog.
     * @param owner Owner window for dialog.
     * @param objectName Object name being deleted.
     * @param objectNameRP Object name being deleted in "Roditelniy" (Genitive) case. Answer the question: "Deletion of what?".
     * @return true if user pressed YES.
     */
    public static boolean showDeleteConfirmDialog(Component owner, String objectName, String objectNameRP)
    {
        return JOptionPane.showConfirmDialog(owner, MessageFormat.format("Вы действительно хотите удалить этот {0}?", objectName),
                MessageFormat.format("Удаление {0}", objectNameRP), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Make AbstractAction dependent from JList selection: enable then something is selected and vise versa.
     * Firstly initializes action state via current selection state. Secondary creates list selection listener.
     * @param action AbstractAction to make dependent.
     * @param list JList to direct action state.
     */
    public static void makeActionDependentFromJList(@NotNull final AbstractAction action, @NotNull final JList list)
    {
        // initialize
        action.setEnabled(!list.isSelectionEmpty());

        // add listener
        list.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                action.setEnabled(!list.isSelectionEmpty());
            }
        });
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
