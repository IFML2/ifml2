package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.forms.expressions.ExpressionEditForm;
import ifml2.editor.gui.forms.expressions.LogicExpressionEditForm;
import ifml2.editor.gui.forms.expressions.TextExpressionEditForm;
import ifml2.om.Property;
import ifml2.om.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class RoleEditor extends AbstractEditor<Role>
{
    private static final String ROLE_EDITOR_TITLE = "Роль";
    private Role roleClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField roleDefinitionText;
    private JList propertiesList;
    private JPanel expressionPanel;
    private JTextArea descriptionTextArea;

    public RoleEditor(@Nullable Window owner, @NotNull Role role)
    {
        super(owner);

        initializeEditor(ROLE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        try
        {
            // clone data
            roleClone = role.clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // list listeners
        propertiesList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                final JList source = (JList) e.getSource();
                final Property selectedValue = (Property) source.getSelectedValue();
                if (selectedValue != null)
                {
                    expressionPanel.removeAll();
                    final String expression = selectedValue.getValueExpression();
                    switch (selectedValue.findDefinition().getType())
                    {
                        case TEXT:
                            TextExpressionEditForm textExpressionEditForm = new TextExpressionEditForm(expression);
                            addExpressionEditForm(textExpressionEditForm);
                            break;
                        case NUMBER:
                            //fixme fill
                            break;
                        case LOGIC:
                            LogicExpressionEditForm logicExpressionEditForm = new LogicExpressionEditForm(expression);
                            addExpressionEditForm(logicExpressionEditForm);
                            break;
                        case COLLECTION:
                            //fixme fill
                            break;
                    }
                }
            }

            private void addExpressionEditForm(ExpressionEditForm expressionEditForm)
            {
                expressionPanel.add(expressionEditForm.getContentPane(),
                        new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
            }
        });

        // bind data
        bindData();
    }

    private void bindData()
    {
        roleDefinitionText.setText(roleClone.getName());
        descriptionTextArea.setText(roleClone.getRoleDefinition().getDescription());
        propertiesList.setModel(new DefaultEventListModel<Property>(roleClone.getProperties()));
        propertiesList.setSelectedIndex(0);
    }

    @Override
    public void getData(@NotNull Role data) throws IFML2EditorException
    {
        //fixme getData
    }
}
