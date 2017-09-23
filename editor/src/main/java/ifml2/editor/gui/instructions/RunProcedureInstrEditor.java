package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.RunProcedureInstruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RunProcedureInstrEditor extends AbstractInstrEditor {
    private static final String RUN_PROCEDURE_EDITOR_TITLE = "Вызвать процедуру";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox procedureCombo;
    private JTextField returnToVarText;
    private JList paramsList;
    private JTextField paramValueText;
    @Nullable
    private EventList<Procedure.FilledParameter> filledParameters;

    public RunProcedureInstrEditor(Window owner, final RunProcedureInstruction runProcedureInstruction, Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(RUN_PROCEDURE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // load data and set dependencies
        paramsList.addListSelectionListener(new ListSelectionListener() // react on parameter selection
        {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Parameter selectedParameter = (Parameter) paramsList.getSelectedValue();
                if (selectedParameter != null) {
                    Procedure.FilledParameter filledParameter = getFilledParameterByName(selectedParameter.getName());
                    if (filledParameter != null) {
                        paramValueText.setText(filledParameter.getValueExpression());
                        paramValueText.setEnabled(true);
                    }
                } else {
                    paramValueText.setText(null);
                }
            }
        });

        procedureCombo.addActionListener(new AbstractAction() // react on procedure selection
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                Procedure procedure = (Procedure) procedureCombo.getSelectedItem();
                if (procedure != null) {
                    EventList<Parameter> parameters = procedure.getParameters();
                    paramsList.setModel(new DefaultEventListModel<Parameter>(parameters));
                    filledParameters = new BasicEventList<Procedure.FilledParameter>(parameters.size());
                    for (Parameter parameter : parameters) {
                        String name = parameter.getName();
                        Procedure.FilledParameter parameterByName = runProcedureInstruction.getParameterByName(name);
                        // if there are parameters in edited instruction then get them, else take empty
                        if (parameterByName != null) {
                            filledParameters.add(new Procedure.FilledParameter(name, parameterByName.getValueExpression()));
                        } else {
                            filledParameters.add(new Procedure.FilledParameter(name, ""));
                        }
                    }
                } else {
                    paramsList.setModel(new DefaultEventListModel<Parameter>(new BasicEventList<Parameter>()));
                    filledParameters = null;
                }
                paramValueText.setEnabled(false);
                paramValueText.setText(null);
            }
        });
        procedureCombo.setModel(new DefaultEventComboBoxModel<Procedure>(storyDataHelper.getProcedures())); // load procedures
        procedureCombo.setSelectedItem(runProcedureInstruction.getProcedure()); // select procedure

        paramValueText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                UpdateChange();
            }

            private void UpdateChange() {
                Parameter selectedParameter = (Parameter) paramsList.getSelectedValue();
                if (selectedParameter != null) {
                    Procedure.FilledParameter filledParameter = getFilledParameterByName(selectedParameter.getName());
                    if (filledParameter != null) {
                        filledParameter.setValueExpression(paramValueText.getText());
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                UpdateChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                UpdateChange();
            }
        });

        returnToVarText.setText(runProcedureInstruction.getReturnToVar());
    }

    private Procedure.FilledParameter getFilledParameterByName(String name) {
        if (filledParameters != null) {
            for (Procedure.FilledParameter filledParameter : filledParameters) {
                String filledParameterName = filledParameter.getName();
                if (filledParameterName != null && filledParameter.getName().equalsIgnoreCase(name)) {
                    return filledParameter;
                }
            }
        }

        return null;
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return RunProcedureInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        super.updateData(instruction);

        RunProcedureInstruction runProcedureInstruction = (RunProcedureInstruction) instruction;
        runProcedureInstruction.setProcedure((Procedure) procedureCombo.getSelectedItem());
        runProcedureInstruction.setParameters(filledParameters);
        runProcedureInstruction.setReturnToVar(returnToVarText.getText());
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (procedureCombo.getSelectedItem() == null) {
            throw new DataNotValidException("Должна быть выбрана процедура для выполнения.", procedureCombo);
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
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Процедура:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        procedureCombo = new JComboBox();
        panel3.add(procedureCombo, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Параметры:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("<html>Сохранить результат<br/>в переменную:</html>");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        returnToVarText = new JTextField();
        panel3.add(returnToVarText, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        paramsList = new JList();
        scrollPane1.setViewportView(paramsList);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder("Значение параметра:"));
        paramValueText = new JTextField();
        panel4.add(paramValueText, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        label1.setLabelFor(procedureCombo);
        label3.setLabelFor(returnToVarText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
