package ifml2.editor.gui.instructions;

import ifml2.editor.IFML2EditorException;
import ifml2.om.Story;
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
    private JTextField itemExprText;
    private JTextField toCollectionExprText;
    private JRadioButton itemRadio;
    private JRadioButton itemExprRadio;
    private JComboBox itemCombo;

    public MoveItemInstrEditor(Window owner, MoveItemInstruction instruction, Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(MOVE_ITEM_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // load data
        itemExprText.setText(instruction.getItemExpr());
        toCollectionExprText.setText(instruction.getToCollectionExpr());
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
        moveItemInstruction.setItemExpr(itemExprText.getText());
        moveItemInstruction.setToCollectionExpr(toCollectionExprText.getText());
    }
}
