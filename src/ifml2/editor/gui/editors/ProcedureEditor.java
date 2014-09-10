package ifml2.editor.gui.editors;

import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.ListEditForm;
import ifml2.editor.gui.instructions.InstructionTypeEnum;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.Word;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

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

        paramsEditForm.init(this, procedureClone.getParameters(), "параметр", "параметра", Word.GenderEnum.MASCULINE,
            false, new Callable<Parameter>()
            {
                @Override
                public Parameter call() throws Exception
                {
                    String parameterName = JOptionPane.showInputDialog(ProcedureEditor.this,
                        "Название нового параметра:", "Новый параметр", JOptionPane.QUESTION_MESSAGE);

                    if (parameterName != null && !"".equals(parameterName))
                    {
                        return new Parameter(parameterName);
                    }
                    return null;
                }
            }, new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    Parameter selectedParam = paramsEditForm.getSelectedElement();
                    if (selectedParam != null)
                    {
                        String parameterName = selectedParam.getName();
                        parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название параметра:",
                            parameterName);
                        if (parameterName != null && !"".equals(parameterName))
                        {
                            selectedParam.setName(parameterName);
                            return true;
                        }
                    }
                    return false;
                }
            });

        instructionsEditForm.init(this, procedureClone.getInstructions(), "инструкцию", "инструкции",
            Word.GenderEnum.FEMININE, true, new Callable<Instruction>()
            {
                @Override
                public Instruction call() throws Exception
                {
                    InstructionTypeEnum instrType = EditorUtils.askInstructionType(ProcedureEditor.this);

                    if (instrType != null)
                    {
                        Instruction instruction = instrType.createInstrInstance();
                        if (EditorUtils.showAssociatedEditor(ProcedureEditor.this, instruction, storyDataHelper))
                        {
                            return instruction;
                        }
                    }
                    return null;
                }
            }, new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    Instruction selectedInstr = instructionsEditForm.getSelectedElement();
                    return selectedInstr != null && EditorUtils.showAssociatedEditor(ProcedureEditor.this,
                        selectedInstr, storyDataHelper);
                }
            });
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
}
