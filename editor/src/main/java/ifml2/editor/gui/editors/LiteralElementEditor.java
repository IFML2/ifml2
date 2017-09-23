package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.LiteralTemplateElement;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ifml2.om.Word.Gender.MASCULINE;

public class LiteralElementEditor extends AbstractEditor<LiteralTemplateElement> {
    private static final String EDITOR_TITLE = "Литерал";
    private static final String PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG = "Если выставлена галочка передачи параметра, то параметр должен быть выбран.";
    private final EventList<String> synonymsClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList synonymsList;
    private JButton addButton;
    private JButton editButton;
    private JButton delButton;
    private JComboBox comboParameter;
    private JCheckBox checkUseParameter;

    public LiteralElementEditor(Window owner, LiteralTemplateElement element, Procedure procedure) {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // set actions
        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String synonym = JOptionPane.showInputDialog(LiteralElementEditor.this, "Введите синоним:");
                if (synonym != null && !"".equals(synonym)) {
                    synonymsClone.add(synonym);
                    synonymsList.setSelectedValue(synonym, true);
                }
            }
        });
        editButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                GUIUtils.makeActionDependentFromJList(this, synonymsList);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                editCurrentSynonym();
            }
        });
        delButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                GUIUtils.makeActionDependentFromJList(this, synonymsList);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSynonym = (String) synonymsList.getSelectedValue();
                if (selectedSynonym != null && GUIUtils.showDeleteConfirmDialog(LiteralElementEditor.this, "синоним", "синонима",
                        MASCULINE)) {
                    synonymsClone.remove(selectedSynonym);
                }
            }
        });

        // listeners
        checkUseParameter.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                comboParameter.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        synonymsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editCurrentSynonym();
                }
            }
        });

        // clone data
        synonymsClone = GlazedLists.eventList(element.getSynonyms());

        // load data
        synonymsList.setModel(new DefaultEventListModel<String>(synonymsClone));

        if (procedure != null) {
            comboParameter.setModel(new DefaultEventComboBoxModel<Parameter>(procedure.getParameters()));
        }
        String parameter = element.getParameter();
        if (procedure != null && parameter != null) {
            checkUseParameter.setSelected(true);
            comboParameter.setSelectedItem(procedure.getParameterByName(parameter));
        } else {
            checkUseParameter.setSelected(false);
        }
    }

    private void editCurrentSynonym() {
        String selectedSynonym = (String) synonymsList.getSelectedValue();
        if (selectedSynonym != null) {
            String editedSynonym = JOptionPane.showInputDialog(this, "Исправьте синоним:", selectedSynonym);
            if (editedSynonym != null && !"".equals(editedSynonym)) {
                synonymsClone.set(synonymsList.getSelectedIndex(), editedSynonym);
            }
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check if check box is set then parameter is selected
        if (checkUseParameter.isSelected() && comboParameter.getSelectedItem() == null) {
            throw new DataNotValidException(PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG, comboParameter);
        }
    }

    @Override
    public void updateData(@NotNull LiteralTemplateElement data) throws IFML2EditorException {
        EventList<String> synonyms = data.getSynonyms();
        synonyms.clear();
        synonyms.addAll(synonymsClone);
        Parameter selectedItem = (Parameter) comboParameter.getSelectedItem();
        data.setParameter(checkUseParameter.isSelected() && selectedItem != null ? selectedItem.toString() : null);
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
        contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setPreferredSize(new Dimension(640, 480));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonCancel.setMnemonic('C');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        synonymsList = new JList();
        scrollPane1.setViewportView(synonymsList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        contentPane.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addButton = new JButton();
        addButton.setText("Добавить...");
        addButton.setMnemonic('Д');
        addButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(addButton);
        editButton = new JButton();
        editButton.setText("Редактировать...");
        editButton.setMnemonic('Р');
        editButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(editButton);
        delButton = new JButton();
        delButton.setText("Удалить");
        delButton.setMnemonic('У');
        delButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(delButton);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        comboParameter = new JComboBox();
        panel4.add(comboParameter, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Параметр в процедуре:");
        panel4.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkUseParameter = new JCheckBox();
        checkUseParameter.setText("Передавать значение элемента в процедуру как параметр");
        checkUseParameter.setMnemonic('П');
        checkUseParameter.setDisplayedMnemonicIndex(0);
        panel4.add(checkUseParameter, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(comboParameter);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
