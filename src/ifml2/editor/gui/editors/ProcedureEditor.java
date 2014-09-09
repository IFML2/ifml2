package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.editor.gui.InstructionsEditForm;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ProcedureEditor extends AbstractEditor<Procedure>
{
    private static final String PROCEDURE_EDITOR_TITLE = "Процедура";
    private Procedure procedureClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JList parametersList;
    private JButton addParameterButton;
    private JButton editParameterButton;
    private JButton deleteParameterButton;
    private InstructionsEditForm instructionsEditForm;
    private Story.DataHelper storyDataHelper;

    public ProcedureEditor(Window owner, @NotNull final Procedure procedure, Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(PROCEDURE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        this.storyDataHelper = storyDataHelper;

        // init buttons
        initButtons();

        try
        {
            // clone data
            procedureClone = procedure.clone();

            // bind data
            bindData();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }
    }

    private void bindData() throws CloneNotSupportedException
    {
        nameText.setText(procedureClone.getName());
        parametersList.setModel(new DefaultEventListModel<Parameter>(procedureClone.getParameters()));
        instructionsEditForm.init(this, procedureClone.getProcedureBody(), storyDataHelper);
    }

    private void initButtons()
    {
        addParameterButton.setAction(new ButtonAction(addParameterButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String parameterName = JOptionPane
                        .showInputDialog(ProcedureEditor.this, "Название нового параметра:", "Новый параметр", JOptionPane.QUESTION_MESSAGE);

                if(parameterName != null && !"".equals(parameterName))
                {
                    Parameter parameter = new Parameter(parameterName);
                    procedureClone.getParameters().add(parameter);
                    parametersList.setSelectedValue(parameter, true);
                }
            }
        });
        editParameterButton.setAction(new ButtonAction(editParameterButton, parametersList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Parameter parameter = (Parameter) parametersList.getSelectedValue();
                if (parameter != null)
                {
                    String parameterName = parameter.getName();
                    parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название параметра:", parameterName);
                    if (parameterName != null && !"".equals(parameterName))
                    {
                        parameter.setName(parameterName);
                        parametersList.setSelectedValue(parameter, true);
                    }
                }
            }
        });
        deleteParameterButton.setAction(new ButtonAction(deleteParameterButton, parametersList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Parameter parameter = (Parameter) parametersList.getSelectedValue();
                if (parameter != null &&
                    GUIUtils.showDeleteConfirmDialog(ProcedureEditor.this, "параметр", "параметра", Word.GenderEnum.MASCULINE))
                {
                    procedureClone.getParameters().remove(parameter);
                }
            }
        });


    }

    @Override
    public void getData(@NotNull Procedure data) throws IFML2EditorException
    {
        try
        {
            // copy data from form to procedureClone
            procedureClone.setName(nameText.getText());
            instructionsEditForm.saveInstructions(procedureClone.getProcedureBody());

            // copy data from procedureClone to procedure
            procedureClone.copyTo(data);
        }
        catch (CloneNotSupportedException e)
        {
            throw new IFML2EditorException("Ошибка при сохранении объекта: {0}", e.getMessage());
        }
    }
}
