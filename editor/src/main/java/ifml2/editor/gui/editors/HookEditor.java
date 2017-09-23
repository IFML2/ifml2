package ifml2.editor.gui.editors;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.InstructionList;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;

import static ifml2.om.Hook.Type.AFTER;
import static ifml2.om.Hook.Type.BEFORE;
import static ifml2.om.Hook.Type.INSTEAD;

public class HookEditor extends AbstractEditor<Hook> {
    private static final String HOOK_EDITOR_TITLE = "Перехват";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox actionCombo;
    private JComboBox parameterCombo;
    private JRadioButton beforeRadio;
    private JRadioButton insteadRadio;
    private JRadioButton afterRadio;
    private JButton editInstructionsButton;
    private JList instructionsList;
    // data to edit
    private InstructionList instructionListClone; // no need to clone - InstructionList isn't modified here

    public HookEditor(Window owner, @NotNull Hook hook, final boolean areObjectHooks, final Story.DataHelper storyDataHelper) throws IFML2EditorException {
        super(owner);
        initializeEditor(HOOK_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- form actions init --

        editInstructionsButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstructionsEditor instructionsEditor = new InstructionsEditor(HookEditor.this, instructionListClone,
                        storyDataHelper);
                if (instructionsEditor.showDialog()) {
                    instructionsEditor.updateData(instructionListClone);
                }
            }
        });

        // -- data init --
        try {
            instructionListClone = hook.getInstructionList().clone();
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }

        //  -- form init --

        // check object hooks or not
        if (!areObjectHooks) {
            parameterCombo.setVisible(false);
        }

        // load parameters and current parameter after action select
        actionCombo.addActionListener(new ActionListener() {
            Action prevSelectedAction;

            @Override
            public void actionPerformed(ActionEvent e) {
                Action selectedAction = (Action) actionCombo.getSelectedItem();
                if (prevSelectedAction != selectedAction) {
                    prevSelectedAction = selectedAction;
                    if (parameterCombo.isVisible()) {
                        parameterCombo.setModel(new DefaultComboBoxModel(selectedAction.getAllObjectParameters()));
                        if (parameterCombo.getItemCount() > 0) // if there are elements ...
                        {
                            parameterCombo.setSelectedIndex(0); // ... select first element
                        }
                    }
                }
            }
        });

        // filter actions due to areObjectHooks
        actionCombo.setModel(new DefaultEventComboBoxModel<Action>(new FilterList<Action>(storyDataHelper.getAllActions(), new Matcher<Action>() {
            @Override
            public boolean matches(Action item) {
                return areObjectHooks && item.getAllObjectParameters().length > 0 || !areObjectHooks;
            }
        })));


        if (hook.getAction() != null) {
            actionCombo.setSelectedItem(hook.getAction()); // select hook's action
            if (hook.getObjectElement() != null) // if hook has assigned objectElement
            {
                parameterCombo.setSelectedItem(hook.getObjectElement());
            }
        } else if (actionCombo.getItemCount() > 0) // if hook's action is null (for new hook e.g.) ...
        {
            actionCombo.setSelectedIndex(0); // ... select first
        }

        // set radio
        switch (hook.getType()) {
            case BEFORE:
                beforeRadio.setSelected(true);
                break;
            case AFTER:
                afterRadio.setSelected(true);
                break;
            case INSTEAD:
                insteadRadio.setSelected(true);
                break;
            default:
                throw new IFML2EditorException(MessageFormat.format("Unknown hook type: {0}", hook.getType()));
        }

        instructionsList.setModel(new DefaultEventListModel<Instruction>(instructionListClone.getInstructions()));
        instructionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Instruction instruction = (Instruction) instructionsList.getSelectedValue();
                    if (instruction != null) {
                        EditorUtils.showAssociatedEditor(HookEditor.this, instruction, storyDataHelper);
                    }
                }
            }
        });
    }

    @Override
    public void updateData(@NotNull Hook hook) throws IFML2EditorException {
        hook.setAction((Action) actionCombo.getSelectedItem());
        hook.setObjectElement((String) parameterCombo.getSelectedItem());
        if (beforeRadio.isSelected()) {
            hook.setType(BEFORE);
        } else if (afterRadio.isSelected()) {
            hook.setType(AFTER);
        } else if (insteadRadio.isSelected()) {
            hook.setType(INSTEAD);
        } else {
            throw new IFML2EditorException("No hook type selected!");
        }

        hook.getInstructionList().replaceInstructions(instructionListClone);
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
        contentPane.setPreferredSize(new Dimension(350, 350));
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
        panel3.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        actionCombo = new JComboBox();
        panel3.add(actionCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Действие:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parameterCombo = new JComboBox();
        panel3.add(parameterCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Параметр:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Тип перехвата:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        beforeRadio = new JRadioButton();
        beforeRadio.setSelected(true);
        beforeRadio.setText("до действия");
        beforeRadio.setMnemonic('О');
        beforeRadio.setDisplayedMnemonicIndex(1);
        panel4.add(beforeRadio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        insteadRadio = new JRadioButton();
        insteadRadio.setText("вместо действия");
        insteadRadio.setMnemonic('В');
        insteadRadio.setDisplayedMnemonicIndex(0);
        panel4.add(insteadRadio, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        afterRadio = new JRadioButton();
        afterRadio.setSelected(false);
        afterRadio.setText("после действия");
        afterRadio.setMnemonic('С');
        afterRadio.setDisplayedMnemonicIndex(2);
        panel4.add(afterRadio, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Реакция:");
        panel3.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        editInstructionsButton = new JButton();
        editInstructionsButton.setText("Редактировать инструкции...");
        editInstructionsButton.setMnemonic('Р');
        editInstructionsButton.setDisplayedMnemonicIndex(0);
        panel5.add(editInstructionsButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        instructionsList = new JList();
        instructionsList.setSelectionMode(0);
        scrollPane1.setViewportView(instructionsList);
        label1.setLabelFor(actionCombo);
        label2.setLabelFor(parameterCombo);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(beforeRadio);
        buttonGroup.add(insteadRadio);
        buttonGroup.add(afterRadio);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
