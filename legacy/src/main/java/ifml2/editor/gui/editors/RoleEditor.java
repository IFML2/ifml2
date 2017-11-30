package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
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
}
