package ifml2.editor.gui.editors;

import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.editor.gui.instructions.InstructionTypeEnum;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.Word;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ProcedureEditor extends AbstractEditor<Procedure>
{
    private static final String PROCEDURE_EDITOR_TITLE = "Процедура";
    private Procedure procedureClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private ListEditForm<Instruction> instructionsEditForm;
    private ListEditForm<Parameter> paramsEditForm;
    private Story.DataHelper storyDataHelper;

    @Override
    protected void validateData() throws DataNotValidException
    {
        // check name for null
        String trimmedName = nameText.getText().trim();

        if (trimmedName.length() == 0)
        {
            throw new DataNotValidException("У процедуры должно быть задано имя.", nameText);
        }

        // check name for duplicates
        Procedure procedure = storyDataHelper.findProcedureById(trimmedName);
        if (procedure != null)
        {
            throw new DataNotValidException("У процедуры должно быть уникальное имя. Процедура с таким именем уже есть в истории.", nameText);
        }
    }

    public ProcedureEditor(Window owner, @NotNull final Procedure procedure, Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(PROCEDURE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        this.storyDataHelper = storyDataHelper;

        try
        {
            // clone data
            procedureClone = procedure.clone();

        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // bind data
        bindData();
    }

    private void bindData()
    {
        nameText.setText(procedureClone.getName());
        paramsEditForm.bindData(procedureClone.getParameters());
        instructionsEditForm.bindData(procedureClone.getInstructions());
    }

    @Override
    public void getData(@NotNull Procedure data) throws IFML2EditorException
    {
        // copy data from form to procedureClone
        procedureClone.setName(nameText.getText());

        try
        {
            // copy data from procedureClone to procedure
            procedureClone.copyTo(data);
        }
        catch (CloneNotSupportedException e)
        {
            throw new IFML2EditorException("Ошибка при сохранении объекта: {0}", e.getMessage());
        }
    }

    private void createUIComponents()
    {
        paramsEditForm = new ListEditForm<Parameter>(this, "параметр", "параметра", Word.GenderEnum.MASCULINE, Parameter.class)
        {
            @Override
            protected Parameter createElement() throws Exception
            {
                String parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название нового параметра:", "Новый параметр",
                        JOptionPane.QUESTION_MESSAGE);
                return parameterName != null && !"".equals(parameterName) ? new Parameter(parameterName) : null;
            }

            @Override
            protected boolean editElement(Parameter selectedElement) throws Exception
            {
                if (selectedElement != null)
                {
                    String parameterName = selectedElement.getName();
                    parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название параметра:", parameterName);
                    if (parameterName != null && !"".equals(parameterName))
                    {
                        selectedElement.setName(parameterName);
                        return true;
                    }
                }
                return false;
            }
        };

        instructionsEditForm = new ListEditForm<Instruction>(this, "инструкцию", "инструкции", Word.GenderEnum.FEMININE, Instruction.class)
        {
            @Override
            protected Instruction createElement() throws Exception
            {
                InstructionTypeEnum instrType = EditorUtils.askInstructionType(ProcedureEditor.this);
                if (instrType != null)
                {
                    Instruction instruction = instrType.createInstrInstance();
                    if (EditorUtils.showAssociatedEditor(owner, instruction, storyDataHelper))
                    {
                        return instruction;
                    }
                }
                return null;
            }

            @Override
            protected boolean editElement(Instruction selectedElement) throws Exception
            {
                return selectedElement != null && EditorUtils.showAssociatedEditor(owner, selectedElement, storyDataHelper);
            }
        };
    }
}
