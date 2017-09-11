package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ifml2.GUIUtils;
import ifml2.om.InstructionList;
import ifml2.om.Story;
import ifml2.om.Word;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

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

    public void init(final Dialog owner, @NotNull InstructionList instructionList, @NotNull Story.DataHelper dataHelper) throws CloneNotSupportedException {
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        instructionsList = new JList();
        instructionsList.setSelectionMode(0);
        scrollPane1.setViewportView(instructionsList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(1);
        panel1.add(toolBar1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        upButton = new JButton();
        upButton.setIcon(new ImageIcon(getClass().getResource("/Up24.gif")));
        upButton.setText("");
        toolBar1.add(upButton);
        downButton = new JButton();
        downButton.setIcon(new ImageIcon(getClass().getResource("/Down24.gif")));
        downButton.setText("");
        toolBar1.add(downButton);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        contentPane.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addInstrButton = new JButton();
        addInstrButton.setIcon(new ImageIcon(getClass().getResource("/Add24.gif")));
        addInstrButton.setText("Добавить...");
        addInstrButton.setMnemonic('Д');
        addInstrButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(addInstrButton);
        editInstrButton = new JButton();
        editInstrButton.setIcon(new ImageIcon(getClass().getResource("/Edit24.gif")));
        editInstrButton.setText("Изменить...");
        editInstrButton.setMnemonic('И');
        editInstrButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(editInstrButton);
        delInstrButton = new JButton();
        delInstrButton.setIcon(new ImageIcon(getClass().getResource("/Delete24.gif")));
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

    /*public void saveInstructions(@NotNull InstructionList instructionList)
    {
        instructionList.replaceInstructions(instructionListClone);
    }*/
}
