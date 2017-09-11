package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonCancel.setMnemonic('C');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        usedLibsList = new JList();
        usedLibsList.setSelectionMode(0);
        scrollPane1.setViewportView(usedLibsList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel3.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addButton = new JButton();
        addButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Add24.gif")));
        addButton.setText("Добавить...");
        addButton.setMnemonic('Д');
        addButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(addButton);
        delButton = new JButton();
        delButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Delete24.gif")));
        delButton.setText("Удалить...");
        delButton.setMnemonic('У');
        delButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(delButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
