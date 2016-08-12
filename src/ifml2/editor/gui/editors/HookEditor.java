package ifml2.editor.gui.editors;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
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
import java.util.Objects;

import static ifml2.om.Hook.Type.*;

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

    HookEditor(Window owner, @NotNull Hook hook, final boolean areObjectHooks, final Story.DataHelper storyDataHelper) throws IFML2EditorException {
        super(owner);
        initializeEditor(HOOK_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- form actions init --

        editInstructionsButton.setAction(new ButtonAction(editInstructionsButton) {
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
                        parameterCombo.setModel(new DefaultComboBoxModel(selectedAction.retrieveAllObjectParameters()));
                        if (parameterCombo.getItemCount() > 0) // if there are elements ...
                        {
                            parameterCombo.setSelectedIndex(0); // ... select first element
                        }
                    }
                }
            }
        });

        // filter actions due to areObjectHooks
        final FilterList<Action> filteredList = new FilterList<>(storyDataHelper.getAllActions(),
                item -> !areObjectHooks || item.retrieveAllObjectParameters().length > 0);
        final SortedList<Action> sortedList = new SortedList<>(filteredList,
                (o1, o2) -> Objects.compare(o1.getName(), o2.getName(), String::compareToIgnoreCase));
        actionCombo.setModel(new DefaultEventComboBoxModel<>(sortedList));

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

        instructionsList.setModel(new DefaultEventListModel<>(instructionListClone.getInstructions()));
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
}
