package ifml2;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.gui.ShowMemoDialog;
import ifml2.om.IFML2LoadXmlException;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.xml.bind.ValidationEvent;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;

public class GUIUtils
{
    private static final String IFML2_EDITOR_GUI_IMAGES = "/ifml2/editor/gui/images/";
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
    public static final Icon DIRECTORY_ICON = getEditorIcon("Open24.gif");
    public static final Icon SAVE_FILE_ICON = getEditorIcon("Save24.gif");
    public static final Icon STORY_FILE_ICON = getEditorIcon("Edit24.gif");
    public static final Icon CIPHERED_STORY_FILE_ICON = STORY_FILE_ICON;
    public static final Icon LIBRARY_FILE_ICON = STORY_FILE_ICON;
    public static final Icon PENCIL_ICON = getEditorIcon("pencil_32.png");
    public static final Icon PALETTE_ICON = getEditorIcon("icons8-paint-palette-24.png");
    public static final Icon MUSIC_FILE_ICON = getEditorIcon("icons8-musical-notes-24.png");

    public static void packAndCenterWindow(@NotNull Window window)
    {
        window.pack();

        // center form
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        // Determine the new location of the window
        int w = window.getSize().width;
        int h = window.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
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
     *
     * @param owner        Owner window for dialog.
     * @param objectNameVP Object name being deleted in "Vinitelniy" (Accusative) case. Answers the question: "Delete what?".
     * @param objectNameRP Object name being deleted in "Roditelniy" (Genitive) case. Answers the question: "Deletion of what?".
     * @param gender       Gender of word.
     * @return true if user pressed YES.
     */
    public static boolean showDeleteConfirmDialog(Component owner, String objectNameVP, String objectNameRP, Word.Gender gender)
    {
        String thisGendered = "";
        switch (gender)
        {
            case MASCULINE:
                thisGendered = "этот";
                break;
            case FEMININE:
                thisGendered = "эту";
                break;
            case NEUTER:
                thisGendered = "это";
                break;
        }
        String question = MessageFormat.format("Вы действительно хотите удалить {0} {1}?", thisGendered, objectNameVP);
        String title = MessageFormat.format("Удаление {0}", objectNameRP);

        return JOptionPane.YES_OPTION ==
               JOptionPane.showConfirmDialog(owner, question, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Make AbstractAction dependent from JList selection: enable then something is selected and vise versa.
     * Firstly initializes action state via current selection state. Secondary creates list selection listener.
     *
     * @param action AbstractAction to make dependent.
     * @param list   JList to direct action state.
     */
    public static void makeActionDependentFromJList(@NotNull final AbstractAction action, @NotNull final JList list)
    {
        // initialize
        action.setEnabled(!list.isSelectionEmpty());

        // add listener
        list.addListSelectionListener(e -> action.setEnabled(!list.isSelectionEmpty()));
    }

    private static ImageIcon getEditorIcon(String fileName)
    {
        return new ImageIcon(GUIUtils.class.getResource(IFML2_EDITOR_GUI_IMAGES + fileName));
    }

    private static void showMemoDialog(Window owner, String title, String message)
    {
        new ShowMemoDialog(owner, title, message);
    }

    public static void ReportError(@NotNull Window owner, @NotNull Throwable exception)
    {
        exception.printStackTrace();
        FormatLogger LOG = new FormatLogger(owner.getClass());
        LOG.error(exception.getMessage());
        StringBuilder errorMessage = new StringBuilder();
        if (!(exception instanceof IFML2LoadXmlException) && exception.getCause() instanceof IFML2LoadXmlException)
        {
            exception = exception.getCause();
        }
        if (exception instanceof IFML2LoadXmlException)
        {
            errorMessage.append("В файле истории есть ошибки:");
            for (ValidationEvent validationEvent : ((IFML2LoadXmlException) exception).getEvents())
            {
                errorMessage.append(MessageFormat
                        .format("\n\"{0}\" at {1},{2}", validationEvent.getMessage(), validationEvent.getLocator().getLineNumber(),
                                validationEvent.getLocator().getColumnNumber()));
            }
        }
        else
        {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            errorMessage.append(stringWriter);
        }
        showMemoDialog(owner, "Произошла ошибка", errorMessage.toString());
    }

    public static @Nullable File selectFile(Component parentComponent, String directory, final String filterName, final String extension, final Icon fileIcon) {
        JFileChooser fileChooser = new JFileChooser(directory);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return filterName;
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(extension);
            }
        });
        fileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                return f.isDirectory() ? GUIUtils.DIRECTORY_ICON : fileIcon;
            }
        });
        if (fileChooser.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    public static @Nullable String inputName(Component parentComponent, String prompt, String initialValue) {
        String name;
        do {
            name = JOptionPane.showInputDialog(parentComponent, prompt, initialValue);
            if (name == null) {
                return null;
            }
            name = name.trim();
            if ("".equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(parentComponent, "Значение не должно быть пустым.", "Пустое значение", JOptionPane.WARNING_MESSAGE);
            }
        } while ("".equalsIgnoreCase(name));

        return name;
    }

    public static <T> @Nullable String inputUniqueName(Component parentComponent, String prompt, Collection<T> uniqueCollection, String duplicateFormatMessage, Function<T, String> nameMapper) {
        boolean isNameNotUnique;
        String name = null;
        do {
            name = inputName(parentComponent, prompt, name);
            if (name == null) {
                return null;
            }

            String finalName = name;
            isNameNotUnique = uniqueCollection.stream().anyMatch(t -> nameMapper.apply(t).equalsIgnoreCase(finalName));
            if (isNameNotUnique) {
                JOptionPane.showMessageDialog(parentComponent, MessageFormat.format(duplicateFormatMessage, name), "Уже есть", JOptionPane.WARNING_MESSAGE);
            }
        } while (isNameNotUnique);
        return name;
    }

    public static class EventComboBoxModelWithNullElement<T> extends DefaultEventComboBoxModel<T>
    {
        public EventComboBoxModelWithNullElement(EventList<T> eventList, T selectedItem)
        {
            super(eventList);
            setSelectedItem(selectedItem);
        }

        // override model to add null element

        @Override
        public int getSize()
        {
            return super.getSize() + 1; // add null element
        }

        @Override
        public Object getElementAt(int index)
        {
            return index == 0 ? null : super.getElementAt(index - 1); // assume element 0 is null
        }
    }
}
