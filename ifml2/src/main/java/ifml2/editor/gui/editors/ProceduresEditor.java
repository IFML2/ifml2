package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ProceduresEditor extends AbstractEditor<HashMap<String, Procedure>> {
    public static final String PROCEDURES_EDITOR_TITLE = "Процедуры";
    private JPanel contentPane;
    private JButton buttonOK;
    private JList proceduresList;
    private JList instructionsList;
    private final AbstractAction delInstructionAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();
            Instruction instruction = (Instruction) instructionsList.getSelectedValue();

            if (procedure != null && instruction != null &&
                    JOptionPane.showConfirmDialog(ProceduresEditor.this, "Вы действительно хотите удалить выбраную инструкцию?",
                            "Удаление инструкции", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                procedure.getInstructions().remove(instruction);
            }
        }
    };
    private JButton addProcedureButton;
    private JButton delProcedureButton;
    private JButton addInstructionButton;
    private JButton editInstructionButton;
    private JButton delInstructionButton;
    private EventList<Procedure> procedures = null;
    private final AbstractAction delProcedureAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();

            if (procedure == null) {
                return;
            }

            if (JOptionPane.showConfirmDialog(ProceduresEditor.this, "Вы действительно хотите удалить процедуру " + procedure.getName() + "?",
                    "Удаление процедуры", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                procedures.remove(procedure);
                updateAllData();
            }
        }
    };
    private Story.DataHelper storyDataHelper;
    private final AbstractAction addInstructionAction = new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();

            if (procedure != null) {
                Instruction.Type instrType = EditorUtils.askInstructionType(ProceduresEditor.this);

                if (instrType != null) {
                    try {
                        Instruction instruction = instrType.createInstrInstance();
                        if (EditorUtils.showAssociatedEditor(ProceduresEditor.this, instruction, storyDataHelper)) {
                            procedure.getInstructions().add(instruction);
                            updateSelectedProcedure();
                            instructionsList.setSelectedValue(instruction, true);
                        }
                    } catch (Throwable ex) {
                        GUIUtils.showErrorMessage(ProceduresEditor.this, ex);
                    }
                }
            }
        }
    };
    private final AbstractAction editInstructionAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Instruction instruction = (Instruction) instructionsList.getSelectedValue();

            if (instruction != null && EditorUtils.showAssociatedEditor(ProceduresEditor.this, instruction, storyDataHelper)) {
                updateSelectedProcedure();
            }
        }
    };

    public ProceduresEditor(Window owner, final EventList<Procedure> procedures, Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(PROCEDURES_EDITOR_TITLE, contentPane, buttonOK, null);
        this.storyDataHelper = storyDataHelper;

        // -- init form --

        addProcedureButton.setAction(new AbstractAction("Новая...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String procedureName = JOptionPane.showInputDialog(ProceduresEditor.this, "Имя новой процедуры:",
                        "Новая процедура", JOptionPane.QUESTION_MESSAGE);

                if (procedureName != null && !"".equals(procedureName)) {
                    Procedure procedure = new Procedure(procedureName);
                    procedures.add(procedure);
                    updateAllData();
                    proceduresList.setSelectedValue(procedure, true);
                }
            }
        });
        delProcedureButton.setAction(delProcedureAction);

        addInstructionButton.setAction(addInstructionAction);
        editInstructionButton.setAction(editInstructionAction);
        delInstructionButton.setAction(delInstructionAction);

        proceduresList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateActions();
                updateSelectedProcedure();
            }
        });

        instructionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateActions();
            }
        });

        updateActions();

        this.procedures = procedures;
        updateAllData();
    }

    @Override
    public void updateData(@NotNull HashMap<String, Procedure> data) throws IFML2EditorException {
        // todo refactor editor to transact mode
    }

    private void updateActions() {
        boolean proceduresCanBeEdited = proceduresList.getSelectedValue() != null;
        delProcedureAction.setEnabled(proceduresCanBeEdited);

        boolean instructionsCanBeEdited = instructionsList.getSelectedValue() != null;
        addInstructionAction.setEnabled(proceduresCanBeEdited);
        editInstructionAction.setEnabled(instructionsCanBeEdited);
        delInstructionAction.setEnabled(instructionsCanBeEdited);
    }

    private void updateSelectedProcedure() {
        Procedure procedure = (Procedure) proceduresList.getSelectedValue();

        if (procedure != null) {
            instructionsList.setModel(new DefaultEventListModel<Instruction>(procedure.getInstructions()));
        }
    }

    private void updateAllData() {
        DefaultListModel proceduresListModel = new DefaultListModel();
        for (Procedure procedure : procedures) {
            proceduresListModel.addElement(procedure);
        }
        proceduresList.setModel(proceduresListModel);
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
        contentPane.setPreferredSize(new Dimension(800, 600));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        panel3.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel4);
        panel4.setBorder(BorderFactory.createTitledBorder("Процедуры"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        proceduresList = new JList();
        scrollPane1.setViewportView(proceduresList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel4.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addProcedureButton = new JButton();
        addProcedureButton.setText("Новая...");
        toolBar1.add(addProcedureButton);
        delProcedureButton = new JButton();
        delProcedureButton.setText("Удалить...");
        delProcedureButton.setMnemonic('У');
        delProcedureButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(delProcedureButton);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel5);
        panel5.setBorder(BorderFactory.createTitledBorder("Инструкции"));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel5.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        instructionsList = new JList();
        scrollPane2.setViewportView(instructionsList);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel5.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addInstructionButton = new JButton();
        addInstructionButton.setText("Новая...");
        toolBar2.add(addInstructionButton);
        editInstructionButton = new JButton();
        editInstructionButton.setText("Редактировать...");
        toolBar2.add(editInstructionButton);
        delInstructionButton = new JButton();
        delInstructionButton.setText("Удалить...");
        toolBar2.add(delInstructionButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
