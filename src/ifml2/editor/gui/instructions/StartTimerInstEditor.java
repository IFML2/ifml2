package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.editors.InstructionsEditor;
import ifml2.om.InstructionList;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.StartTimerInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Duration;

public class StartTimerInstEditor extends AbstractInstrEditor {
    private static final String START_TIMER_EDITOR_TITLE = "⏰ Запустить таймер";
    private final InstructionList instructionListClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton realTimeRadioButton;
    private JRadioButton actionCountRadioButton;
    private JSpinner actionCountSpinner;
    private JSpinner durationSpinner;
    private JList<Instruction> instructionList;
    private JButton editInstructionsButton;

    public StartTimerInstEditor(Window owner, final @NotNull StartTimerInstruction startTimerInstruction, Story.DataHelper storyDataHelper) throws CloneNotSupportedException {
        super(owner);
        initializeEditor(START_TIMER_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // listeners
        ChangeListener radioChangeListener = e -> {
            durationSpinner.setEnabled(realTimeRadioButton.isSelected());
            actionCountSpinner.setEnabled(actionCountRadioButton.isSelected());
        };
        realTimeRadioButton.addChangeListener(radioChangeListener);
        actionCountRadioButton.addChangeListener(radioChangeListener);
        editInstructionsButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstructionsEditor instructionsEditor = new InstructionsEditor(StartTimerInstEditor.this, instructionListClone, storyDataHelper);
                if (instructionsEditor.showDialog())
                {
                    instructionsEditor.updateData(instructionListClone);
                }
            }
        });

        // clone data
        instructionListClone = startTimerInstruction.getInstructions().clone();

        // load data
        switch (startTimerInstruction.getTimerType()) {
            case REAL_TIME:
                realTimeRadioButton.setSelected(true);
                durationSpinner.setValue((int) startTimerInstruction.getDuration().getSeconds());
                break;
            case ACTION_COUNT:
                actionCountRadioButton.setSelected(true);
                actionCountSpinner.setValue(startTimerInstruction.getActionCount());
                break;
        }
        //noinspection unchecked
        instructionList.setModel(new DefaultEventListModel<Instruction>(instructionListClone.getInstructions()));
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return StartTimerInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        super.updateData(instruction);

        StartTimerInstruction startTimerInstruction = (StartTimerInstruction) instruction;
        if (realTimeRadioButton.isSelected()) {
            startTimerInstruction.setTimerType(StartTimerInstruction.Type.REAL_TIME);
            startTimerInstruction.setDuration(Duration.ofSeconds((int) durationSpinner.getValue()));
        } else {
            startTimerInstruction.setTimerType(StartTimerInstruction.Type.ACTION_COUNT);
            startTimerInstruction.setActionCount((int) actionCountSpinner.getValue());
        }
        startTimerInstruction.setInstructions(instructionListClone);
    }
}
