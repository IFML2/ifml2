package ifml2.editor.gui.instructions;

import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.GoToLocInstruction;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class GoToLocInstrEditor extends AbstractInstrEditor
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField locExprText;

    public GoToLocInstrEditor(Window owner, GoToLocInstruction instruction)
    {
        super(owner);
        initializeEditor(GoToLocInstruction.getTitle(), contentPane, buttonOK, buttonCancel);

        //todo set data
    }

    @Override
    protected Class<? extends Instruction> getInstrClass()
    {
        return GoToLocInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException
    {
        getData(instruction);

        // todo get instr
    }
}
