package ifml2.editor.gui;

import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.ObjectTemplateElement;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ObjectElementEditor extends AbstractEditor<ObjectTemplateElement>
{
    private static final String EDITOR_TITLE = "Объект";
    private static final String PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG = "Если выставлена галочка передачи параметра, то параметр должен быть выбран.";
    private static final String SET_PARAMETER_DIALOG_TITLE = "Параметр не задан";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboCase;
    private JComboBox comboParameter;
    private JCheckBox checkUseParameter;
    private ObjectTemplateElement elementClone;

    public ObjectElementEditor(Window owner, @NotNull ObjectTemplateElement element, Procedure procedure)
    {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // add listeners
        checkUseParameter.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                comboParameter.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        // clone data
        try
        {
            elementClone = element.clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // load data

        comboCase.setModel(new DefaultComboBoxModel(Word.GramCaseEnum.values()));
        comboCase.setSelectedItem(elementClone.getGramCase());

        if (procedure != null)
        {
            comboParameter.setModel(new DefaultEventComboBoxModel<Parameter>(procedure.getParameters()));
        }
        String parameter = elementClone.getParameter();
        if(procedure != null && parameter != null)
        {
            checkUseParameter.setSelected(true);
            comboParameter.setSelectedItem(procedure.getParameterByName(parameter));
        }
        else
        {
            checkUseParameter.setSelected(false);
        }
    }

    @Override
    protected boolean validateData()
    {
        // check if check box is set then parameter is selected
        if(checkUseParameter.isSelected() && comboParameter.getSelectedItem() == null)
        {
            JOptionPane.showMessageDialog(this, PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG, SET_PARAMETER_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
            comboParameter.requestFocusInWindow();
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void getData(@NotNull ObjectTemplateElement data) throws IFML2EditorException
    {
        data.setGramCase((Word.GramCaseEnum) comboCase.getSelectedItem());
        Parameter selectedItem = (Parameter) comboParameter.getSelectedItem();
        data.setParameter(checkUseParameter.isSelected() && selectedItem != null ? selectedItem.toString() : null);
    }
}
