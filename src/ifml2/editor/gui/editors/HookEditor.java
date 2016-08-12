package ifml2.editor.gui.editors;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.Story;
import ifml2.om.Word;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Objects;

import static ifml2.om.Hook.Type.*;

public class HookEditor extends AbstractEditor<Hook> {
    private static final String HOOK_EDITOR_TITLE = "Перехват";
    private final Hook hookClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<Action> actionCombo;
    private JComboBox<Object> parameterCombo;
    private JRadioButton beforeRadio;
    private JRadioButton insteadRadio;
    private JRadioButton afterRadio;
    private ListEditForm<Instruction> instructionsListEditForm;
    private Story.DataHelper storyDataHelper;

    HookEditor(Window owner, @NotNull Hook hook, final boolean areObjectHooks, final Story.DataHelper storyDataHelper) throws IFML2EditorException {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor(HOOK_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // clone whole entity
        try {
            hookClone = hook.clone();
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Ошибка при клонировании перехвата: {0}", e.toString());
        }

        // bind data
        bindData();

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
                        parameterCombo.setModel(new DefaultComboBoxModel<>(selectedAction.retrieveAllObjectParameters()));
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

        final Action action = hook.getAction();
        if (action != null) {
            actionCombo.setSelectedItem(action); // select hook's action
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
    }

    private void bindData() {
        instructionsListEditForm.bindData(hookClone.getInstructionList().getInstructions());
    }

    @Override
    public void updateData(@NotNull Hook hook) throws IFML2EditorException {
        hookClone.setAction((Action) actionCombo.getSelectedItem());
        hookClone.setObjectElement((String) parameterCombo.getSelectedItem());
        if (beforeRadio.isSelected()) {
            hookClone.setType(BEFORE);
        } else if (afterRadio.isSelected()) {
            hookClone.setType(AFTER);
        } else if (insteadRadio.isSelected()) {
            hookClone.setType(INSTEAD);
        } else {
            throw new IFML2EditorException("No hook type selected!");
        }
        try {
            hookClone.copyTo(hook);
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Ошибка при копировании перехвата: {0}", e.toString());
        }
    }

    private void createUIComponents() {
        instructionsListEditForm = new ListEditForm<Instruction>(this, "инструкцию", "инструкции", Word.Gender.FEMININE, Instruction.class) {
            @Override
            protected Instruction createElement() throws Exception {
                Instruction.Type instrType = EditorUtils.askInstructionType(HookEditor.this);
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
}