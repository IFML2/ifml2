package ifml2.editor.gui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jetbrains.annotations.NotNull;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.om.InstructionList;
import ifml2.om.Story;
import ifml2.om.Word;
import ifml2.vm.instructions.Instruction;

public class InstructionsEditForm extends JInternalFrame {
    private JPanel contentPane;
    private JList instructionsList;
    private JButton upButton;
    private JButton downButton;
    private JButton addInstrButton;
    private JButton editInstrButton;
    private JButton delInstrButton;
    private InstructionList instructionListClone;
    private Dialog owner;
    private Story.DataHelper storyDataHelper;

    public InstructionsEditForm() {
        setContentPane(contentPane);
    }

    public void init(final Dialog owner, @NotNull InstructionList instructionList, @NotNull Story.DataHelper dataHelper)
            throws CloneNotSupportedException {
        this.owner = owner;
        this.storyDataHelper = dataHelper;
        instructionListClone = instructionList.clone();

        // init buttons
        initButtons();

        // init form
        bindData();
    }

    private void bindData() {
        instructionsList.setModel(new DefaultEventListModel<Instruction>(instructionListClone.getInstructions()));
    }

    private void initButtons() {
        addInstrButton.setAction(new ButtonAction(addInstrButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Instruction.Type instrType = EditorUtils.askInstructionType(owner);
                if (instrType != null) {
                    try {
                        Instruction instruction = instrType.createInstrInstance();
                        if (EditorUtils.showAssociatedEditor(owner, instruction, storyDataHelper)) {
                            instructionListClone.getInstructions().add(instruction);
                            instructionsList.setSelectedValue(instruction, true);
                        }
                    } catch (Throwable ex) {
                        GUIUtils.showErrorMessage(owner, ex);
                    }
                }
            }
        });

        editInstrButton.setAction(new ButtonAction(editInstrButton, instructionsList) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Instruction selectedInstr = (Instruction) instructionsList.getSelectedValue();
                if (selectedInstr != null) {
                    EditorUtils.showAssociatedEditor(owner, selectedInstr, storyDataHelper);
                }
            }
        });

        delInstrButton.setAction(new ButtonAction(delInstrButton, instructionsList) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUIUtils.showDeleteConfirmDialog(owner, "инструкцию", "инструкции", Word.Gender.FEMININE)) {
                    Instruction selectedInstr = (Instruction) instructionsList.getSelectedValue();
                    if (selectedInstr != null) {
                        instructionListClone.getInstructions().remove(selectedInstr);
                    }
                }
            }
        });

        upButton.setAction(new ButtonAction(upButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = instructionsList.getSelectedIndex();
                if (selIdx > 0) {
                    Collections.swap(instructionListClone.getInstructions(), selIdx, selIdx - 1);
                    instructionsList.setSelectedIndex(selIdx - 1);
                }
            }

            @Override
            public void init() {
                updateState();

                instructionsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        updateState();
                    }
                });
            }

            private void updateState() {
                setEnabled(instructionsList.getSelectedIndex() > 0);
            }
        });

        downButton.setAction(new ButtonAction(downButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = instructionsList.getSelectedIndex();
                EventList<Instruction> instructions = instructionListClone.getInstructions();
                if (selIdx < instructions.size() - 1) {
                    Collections.swap(instructions, selIdx, selIdx + 1);
                    instructionsList.setSelectedIndex(selIdx + 1);
                }
            }

            @Override
            public void init() {
                updateState();

                instructionsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        updateState();
                    }
                });
            }

            private void updateState() {
                int selectedInstrIdx = instructionsList.getSelectedIndex();
                int listSize = instructionsList.getModel().getSize();
                setEnabled(selectedInstrIdx < listSize - 1);
            }
        });
    }

    /*
     * public void saveInstructions(@NotNull InstructionList instructionList) {
     * instructionList.replaceInstructions(instructionListClone); }
     */
}
