package ifml2.editor.gui.instructions;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;

import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.ReturnInstruction;

public class ReturnInstrEditor extends AbstractInstrEditor {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField returnExpressionText;

    public ReturnInstrEditor(Window owner, ReturnInstruction instruction) {
        super(owner);
        initializeEditor(Instruction.getTitleFor(ReturnInstruction.class), contentPane, buttonOK, buttonCancel);

        // -- init data --
        returnExpressionText.setText(instruction.getValue());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return ReturnInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        updateData(instruction);

        ReturnInstruction returnInstruction = (ReturnInstruction) instruction;

        returnInstruction.setValue(returnExpressionText.getText());
    }
}
