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
import ifml2.vm.instructions.Type;

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

    public ProcedureEditor(Window owner, /*@NotNull*/ final Procedure procedure, Story.DataHelper storyDataHelper) {
        super(owner);
        $$$setupUI$$$();
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
    public void updateData(/*@NotNull*/ Procedure data) throws IFML2EditorException {
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
                ifml2.vm.instructions.Type instrType = EditorUtils.askInstructionType(ProcedureEditor.this);
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

    protected boolean hasParameter(/*@NotNull*/ String name) {
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setPreferredSize(new Dimension(800, 600));
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
        label1.setText("Название:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameText = new JTextField();
        panel3.add(nameText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder("Инструкции"));
        panel4.add(instructionsEditForm.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Параметры"));
        panel5.add(paramsEditForm.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label1.setLabelFor(nameText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
