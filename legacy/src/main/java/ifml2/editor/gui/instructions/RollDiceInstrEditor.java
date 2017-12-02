package ifml2.editor.gui.instructions;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.jetbrains.annotations.NotNull;

import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.RollDiceInstruction;

public class RollDiceInstrEditor extends AbstractInstrEditor {
    private static final String ROLL_DICE_EDITOR_TITLE = "Бросить кость";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JTextField varText;

    public RollDiceInstrEditor(Window owner, RollDiceInstruction instruction) {
        super(owner);
        initializeEditor(ROLL_DICE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // initialize and load spinners
        fromSpinner
                .setModel(new SpinnerNumberModel(instruction.getFromNumber(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        toSpinner.setModel(new SpinnerNumberModel(instruction.getToNumber(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

        // load var name
        varText.setText(instruction.getVarName());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return RollDiceInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        RollDiceInstruction rollDiceInstruction = (RollDiceInstruction) instruction;
        rollDiceInstruction.setFromNumber(((SpinnerNumberModel) fromSpinner.getModel()).getNumber().intValue());
        rollDiceInstruction.setToNumber(((SpinnerNumberModel) toSpinner.getModel()).getNumber().intValue());
        rollDiceInstruction.setVarName(varText.getText());
    }
}
