package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
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
    private JPanel contentPane;
    private JButton buttonOK;
    private JList proceduresList;
    private JList instructionsList;
    private JButton addProcedureButton;
    private JButton delProcedureButton;
    private JButton addInstructionButton;
    private JButton editInstructionButton;
    private JButton delInstructionButton;

    public static final String PROCEDURES_EDITOR_TITLE = "Процедуры";

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
}
