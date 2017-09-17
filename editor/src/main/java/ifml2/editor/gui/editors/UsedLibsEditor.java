package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Library;
import ifml2.om.OMManager;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class UsedLibsEditor extends AbstractEditor<List<Library>> {
    public static final String USED_LIBS_EDITOR_TITLE = "Используемые библиотеки";
    private JPanel contentPane;
    private JButton buttonOK;
    private JList usedLibsList;
    private JButton addButton;
    private JButton delButton;
    private JButton buttonCancel;

    private EventList<Library> librariesClone;

    /* todo editor is transactional, but it reassigns libs in updateData()
    * now it will be working ?
    * other objects links to previous libs and reassign should be more smart - retain previous libs - we can compare them
    * */

    public UsedLibsEditor(Window owner, final EventList<Library> libraries, final Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(USED_LIBS_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- init actions --
        addButton.addActionListener(e -> {
            JFileChooser libFileChooser = new JFileChooser(CommonUtils.getLibrariesDirectory());
            libFileChooser.setFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return CommonConstants.LIBRARY_FILE_FILTER_NAME;
                }

                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.LIBRARY_EXTENSION);
                }
            });

            libFileChooser.setFileView(new FileView() {
                @Override
                public Icon getIcon(File f) {
                    if (f.isDirectory()) {
                        return GUIUtils.DIRECTORY_ICON;
                    }
                    return GUIUtils.LIBRARY_FILE_ICON;
                }
            });

            if (libFileChooser.showOpenDialog(UsedLibsEditor.this) == JFileChooser.APPROVE_OPTION) {
                Library library;
                try {
                    library = OMManager.loadLibrary(libFileChooser.getSelectedFile());
                    if (!storyDataHelper.isLibListContainsLib(librariesClone, library)) {
                        librariesClone.add(library);
                    } else {
                        JOptionPane.showMessageDialog(UsedLibsEditor.this, "В списке уже есть эта библиотека", "Уже есть",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (IFML2Exception error) {
                    JOptionPane.showMessageDialog(UsedLibsEditor.this,
                            "Произошла ошибка во время загрузки библиотеки: \n" + error.getMessage(),
                            "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                if (library != null) {
                    usedLibsList.setSelectedValue(library, true);
                }
            }
        });
        delButton.setAction(new AbstractAction(delButton.getText(), delButton.getIcon()) {
            {
                setEnabled(false); // disabled at start
                usedLibsList.addListSelectionListener(e -> {
                    setEnabled(!usedLibsList.isSelectionEmpty()); // depends on selection
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Library usedLib = (Library) usedLibsList.getSelectedValue();
                if (usedLib != null) {
                    if (JOptionPane.showConfirmDialog(UsedLibsEditor.this, "Вы уверены, что не хотите больше использовать эту библиотеку?",
                            "Удаление библиотеки", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        librariesClone.remove(usedLib);
                    }
                }
            }
        });

        // init clones
        librariesClone = GlazedLists.eventList(libraries);

        // init controls
        usedLibsList.setModel(new DefaultEventListModel<>(librariesClone));
    }

    @Override
    public void updateData(@NotNull List<Library> libraries) throws IFML2EditorException {
        libraries.clear();
        libraries.addAll(librariesClone);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

}
