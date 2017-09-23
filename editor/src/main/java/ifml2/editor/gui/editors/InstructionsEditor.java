package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.om.InstructionList;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

public class InstructionsEditor extends AbstractEditor<InstructionList> {
    private static final String INSTR_EDITOR_TITLE = "Инструкции";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList instructionsList;
    private JButton addInstrButton;
    private JButton editInstrButton;
    private JButton delInstrButton;
    private JButton upButton;
    private JButton downButton;
    // data to clone
    private InstructionList instructionListClone;
    private final AbstractAction delInstrAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(InstructionsEditor.this, "Вы действительно хотите удалить выбранную инструкцию?",
                    "Удаление инструкции", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                Instruction selectedInstr = (Instruction) instructionsList.getSelectedValue();
                if (selectedInstr != null) {
                    instructionListClone.getInstructions().remove(selectedInstr);
                }
            }
        }
    };
    private final AbstractAction upAction = new AbstractAction("", GUIUtils.UP_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selIdx = instructionsList.getSelectedIndex();
            if (selIdx > 0) {
                Collections.swap(instructionListClone.getInstructions(), selIdx, selIdx - 1);
                instructionsList.setSelectedIndex(selIdx - 1);
            }
        }
    };
    private final AbstractAction downAction = new AbstractAction("", GUIUtils.DOWN_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selIdx = instructionsList.getSelectedIndex();
            EventList<Instruction> instructions = instructionListClone.getInstructions();
            if (selIdx < instructions.size() - 1) {
                Collections.swap(instructions, selIdx, selIdx + 1);
                instructionsList.setSelectedIndex(selIdx + 1);
            }
        }
    };
    private Story.DataHelper storyDataHelper;
    private final AbstractAction editInstrAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Instruction selectedInstr = (Instruction) instructionsList.getSelectedValue();
            if (selectedInstr != null) {
                EditorUtils.showAssociatedEditor(InstructionsEditor.this, selectedInstr, storyDataHelper);
            }
        }
    };

    public InstructionsEditor(Window owner, final InstructionList instructionList, final Story.DataHelper storyDataHelper) {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor(INSTR_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- form actions init --

        addInstrButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ifml2.vm.instructions.Type instrType = EditorUtils.askInstructionType(InstructionsEditor.this);
                if (instrType != null) {
                    try {
                        Instruction instruction = instrType.createInstrInstance();
                        if (EditorUtils.showAssociatedEditor(InstructionsEditor.this, instruction, storyDataHelper)) {
                            instructionListClone.getInstructions().add(instruction);
                            instructionsList.setSelectedValue(instruction, true);
                        }
                    } catch (Throwable ex) {
                        GUIUtils.showErrorMessage(InstructionsEditor.this, ex);
                    }
                }
            }
        });
        editInstrButton.setAction(editInstrAction);
        delInstrButton.setAction(delInstrAction);
        upButton.setAction(upAction);
        downButton.setAction(downAction);

        // -- clone data --

        try {
            instructionListClone = instructionList.clone();
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }

        // -- init form --

        instructionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                UpdateActions();
            }
        });
        instructionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Instruction instruction = (Instruction) instructionsList.getSelectedValue();
                    if (instruction != null) {
                        EditorUtils.showAssociatedEditor(InstructionsEditor.this, instruction, storyDataHelper);
                    }
                }
            }
        });
        instructionsList.setModel(new DefaultEventListModel<Instruction>(instructionListClone.getInstructions()));

        UpdateActions();
    }

    private void UpdateActions() {
        boolean isInstrSelected = instructionsList.getSelectedValue() != null;
        editInstrAction.setEnabled(isInstrSelected);
        delInstrAction.setEnabled(isInstrSelected);

        int selectedInstrIdx = instructionsList.getSelectedIndex();
        upAction.setEnabled(isInstrSelected && selectedInstrIdx > 0);
        downAction.setEnabled(isInstrSelected && selectedInstrIdx < instructionsList.getModel().getSize() - 1);
    }

    @Override
    public void updateData(@NotNull InstructionList instructionList) {
        instructionList.replaceInstructions(instructionListClone);
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
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setPreferredSize(new Dimension(640, 300));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
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
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        instructionsList = new JList();
        instructionsList.setSelectionMode(0);
        scrollPane1.setViewportView(instructionsList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(1);
        panel3.add(toolBar1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        upButton = new JButton();
        upButton.setText("Вверх");
        upButton.setMnemonic('В');
        upButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(upButton);
        downButton = new JButton();
        downButton.setText("Вниз");
        downButton.setMnemonic('Н');
        downButton.setDisplayedMnemonicIndex(1);
        toolBar1.add(downButton);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        contentPane.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addInstrButton = new JButton();
        addInstrButton.setText("Добавить...");
        addInstrButton.setMnemonic('Д');
        addInstrButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(addInstrButton);
        editInstrButton = new JButton();
        editInstrButton.setText("Изменить...");
        editInstrButton.setMnemonic('И');
        editInstrButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(editInstrButton);
        delInstrButton = new JButton();
        delInstrButton.setText("Удалить...");
        delInstrButton.setMnemonic('У');
        delInstrButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(delInstrButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
