package ifml2.editor.gui.editors;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static ifml2.om.Word.Gender.FEMININE;
import static ifml2.om.Word.Gender.MASCULINE;

public class ProcedureEditor extends AbstractEditor<Procedure> {
    private static final String PROCEDURE_EDITOR_TITLE = "Процедура";
    protected ListEditForm<Parameter> paramsEditForm;
    private Procedure procedureClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private ListEditForm<Instruction> instructionsEditForm;
    private Story.DataHelper storyDataHelper;
    private Procedure originalProcedure;

    public ProcedureEditor(Window owner, @NotNull final Procedure procedure, Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(PROCEDURE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        originalProcedure = procedure;
        this.storyDataHelper = storyDataHelper;

        try {
            // clone data
            procedureClone = procedure.clone();
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }

        // bind data
        bindData();
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check name for null
        String trimmedName = nameText.getText().trim();

        if (trimmedName.length() == 0) {
            throw new DataNotValidException("У процедуры должно быть задано имя.", nameText);
        }

        // check name for duplicates
        Procedure procedure = storyDataHelper.findProcedureById(trimmedName);
        if (procedure != null && !procedure.equals(originalProcedure)) {
            throw new DataNotValidException("У процедуры должно быть уникальное имя. Процедура с таким именем уже есть в истории.",
                    nameText);
        }
    }

    private void bindData() {
        nameText.setText(procedureClone.getName());
        paramsEditForm.bindData(procedureClone.getParameters());
        instructionsEditForm.bindData(procedureClone.getInstructions());
    }

    @Override
    public void updateData(@NotNull Procedure data) throws IFML2EditorException {
        // copy data from form to procedureClone
        procedureClone.setName(nameText.getText());

        try {
            // copy data from procedureClone to procedure
            procedureClone.copyTo(data);
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Ошибка при сохранении объекта: {0}", e.getMessage());
        }
    }

    private void createUIComponents() {
        paramsEditForm = new ListEditForm<Parameter>(this, "параметр", "параметра", MASCULINE, Parameter.class) {
            @Override
            protected Parameter createElement() throws Exception {
                String parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название нового параметра:", "Новый параметр",
                        JOptionPane.QUESTION_MESSAGE);
                return parameterName != null && !"".equals(parameterName) ? new Parameter(parameterName) : null;
            }

            @Override
            protected boolean editElement(Parameter selectedElement) throws Exception {
                if (selectedElement != null) {
                    String parameterName = selectedElement.getName();
                    parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название параметра:", parameterName);
                    if (parameterName != null && !"".equals(parameterName)) {
                        selectedElement.setName(parameterName);
                        return true;
                    }
                }
                return false;
            }
        };

        instructionsEditForm = new ListEditForm<Instruction>(this, "инструкцию", "инструкции", FEMININE, Instruction.class) {
            @Override
            protected Instruction createElement() throws Exception {
                Instruction.Type instrType = EditorUtils.askInstructionType(ProcedureEditor.this);
                if (instrType != null) {
                    Instruction instruction = instrType.createInstrInstance();
                    if (EditorUtils.showAssociatedEditor(owner, instruction, storyDataHelper)) {
                        return instruction;
                    }
                }
                return null;
            }

            @Override
            protected boolean editElement(Instruction selectedElement) throws Exception {
                return selectedElement != null && EditorUtils.showAssociatedEditor(owner, selectedElement, storyDataHelper);
            }
        };
    }

    protected boolean hasParameter(@NotNull String name) {
        for (Parameter parameter : paramsEditForm.getClonedList()) {
            if (name.equalsIgnoreCase(parameter.getName())) {
                return true;
            }
        }

        return false;
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
