package ifml2.editor.gui.instructions;

import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.MoveItemInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MoveItemInstrEditor extends AbstractInstrEditor
{
    private static final String MOVE_ITEM_EDITOR_TITLE = MoveItemInstruction.getTitle();
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox itemCombo;
    private JComboBox toCombo;

    public MoveItemInstrEditor(Window owner, MoveItemInstruction instruction)
    {
        super(owner);
        initializeEditor(MOVE_ITEM_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // load all available data
        //todo

        // load data
        //todo itemCombo.
    }

    @Override
    protected Class<? extends Instruction> getInstrClass()
    {
        return MoveItemInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException
    {
        getData(instruction);

        MoveItemInstruction moveItemInstruction = (MoveItemInstruction) instruction;
        //todo
    }
}
