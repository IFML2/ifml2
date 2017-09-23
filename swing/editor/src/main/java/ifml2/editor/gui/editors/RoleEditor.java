package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.forms.expressions.CollectionEditForm;
import ifml2.editor.gui.forms.expressions.ExpressionEditForm;
import ifml2.editor.gui.forms.expressions.LogicExpressionEditForm;
import ifml2.editor.gui.forms.expressions.NumberExpressionEditForm;
import ifml2.editor.gui.forms.expressions.TextExpressionEditForm;
import ifml2.om.Item;
import ifml2.om.Property;
import ifml2.om.Role;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class RoleEditor extends AbstractEditor<Role> {
    private static final String ROLE_EDITOR_TITLE = "Роль";
    private Role roleClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField roleDefinitionText;
    private JList propertiesList;
    private JPanel expressionPanel;
    private JTextArea descriptionTextArea;
    private Property currentProperty;
    private JInternalFrame currentForm;
    private Item holder;

    public RoleEditor(@Nullable Window owner, @NotNull Role role, @NotNull Item holder, final Story.DataHelper dataHelper) {
        super(owner);
        this.holder = holder;

        initializeEditor(ROLE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        try {
            // clone data
            roleClone = role.clone();
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }

        // list listeners
        propertiesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    saveCurrentProperty();
                } catch (IFML2EditorException ex) {
                    GUIUtils.showErrorMessage(RoleEditor.this, ex);
                }

                final JList source = (JList) e.getSource();
                currentProperty = (Property) source.getSelectedValue();

                if (currentProperty != null) {
                    final String expression = currentProperty.getValueExpression();
                    switch (currentProperty.findDefinition().getType()) {
                        case TEXT:
                            changeEditForm(new TextExpressionEditForm(expression));
                            break;
                        case NUMBER:
                            changeEditForm(new NumberExpressionEditForm(expression));
                            break;
                        case LOGIC:
                            changeEditForm(new LogicExpressionEditForm(expression));
                            break;
                        case COLLECTION:
                            changeEditForm(new CollectionEditForm(RoleEditor.this, currentProperty.getCollectionItems(), Item.class,
                                    RoleEditor.this.holder, dataHelper));
                            break;
                    }
                }
            }

            private void changeEditForm(JInternalFrame expressionEditForm) {
                currentForm = expressionEditForm;
                expressionPanel.removeAll();
                expressionPanel.add(expressionEditForm.getContentPane(),
                        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
            }
        });

        // bind data
        bindData();
    }

    private void saveCurrentProperty() throws IFML2EditorException {
        if (currentProperty != null && currentForm != null) {
            if (currentForm instanceof ExpressionEditForm) {
                ExpressionEditForm expressionEditForm = (ExpressionEditForm) currentForm;
                currentProperty.setValueExpression(expressionEditForm.getEditedExpression());
            } else if (currentForm instanceof CollectionEditForm) {
                CollectionEditForm collectionEditForm = (CollectionEditForm) currentForm;
                currentProperty.setCollectionItems(collectionEditForm.getEditedCollection());
            } else {
                throw new IFML2EditorException("Неизвестный тип формы для свойства: {0}", currentForm.getClass().getName());
            }
        }
    }

    private void bindData() {
        roleDefinitionText.setText(roleClone.getName());
        descriptionTextArea.setText(roleClone.getRoleDefinition().getDescription());
        propertiesList.setModel(new DefaultEventListModel<Property>(roleClone.getProperties()));
        propertiesList.setSelectedIndex(0);
    }

    @Override
    public void updateData(@NotNull Role role) throws IFML2EditorException {
        try {
            saveCurrentProperty();
            roleClone.copyTo(role);
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Внутренняя ошибка: {0}", e.getMessage());
        }
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
        contentPane.setPreferredSize(new Dimension(500, 400));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
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
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setRequestFocusEnabled(false);
        label1.setText("Роль:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        roleDefinitionText = new JTextField();
        roleDefinitionText.setEditable(false);
        panel3.add(roleDefinitionText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder("Свойства"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(213, 128), null, 0, false));
        propertiesList = new JList();
        propertiesList.setSelectionMode(0);
        scrollPane1.setViewportView(propertiesList);
        expressionPanel = new JPanel();
        expressionPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(expressionPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        expressionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final JLabel label2 = new JLabel();
        label2.setText("Описание:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel3.add(scrollPane2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        scrollPane2.setViewportView(descriptionTextArea);
        label1.setLabelFor(roleDefinitionText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
