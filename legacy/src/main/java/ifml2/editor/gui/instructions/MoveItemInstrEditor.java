package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Item;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.MoveItemInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MoveItemInstrEditor extends AbstractInstrEditor
{
    private static final String MOVE_ITEM_EDITOR_TITLE = Instruction.getTitleFor(MoveItemInstruction.class);
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

        // set listeners
        ChangeListener radioChangeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                itemCombo.setEnabled(itemRadio.isSelected());
                itemExprText.setEnabled(itemExprRadio.isSelected());
            }
        };
        itemRadio.addChangeListener(radioChangeListener);
        itemExprRadio.addChangeListener(radioChangeListener);
        itemCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = (Item) itemCombo.getSelectedItem();
                if(item != null)
                {
                    itemExprText.setText(item.getId());
                }
            }
        });

        // load data
        String itemExpr = instruction.getItemExpr();
        itemCombo.setModel(new DefaultEventComboBoxModel<Item>(storyDataHelper.getItems()));
        itemExprText.setText(instruction.getItemExpr());
        // detect if item expression is item id
        Item item = storyDataHelper.findItemById(itemExpr);
        if(item != null || "".equals(itemExpr)) // item by id is found or expression is empty (for new instruction)
        {
            itemRadio.setSelected(true);
            itemCombo.setSelectedItem(item);
        }
        else
        {
            itemExprRadio.setSelected(true);
        }
        itemExprText.setText(itemExpr);

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
        updateData(instruction);

        MoveItemInstruction moveItemInstruction = (MoveItemInstruction) instruction;

        // set item expr
        if (itemRadio.isSelected())
        {
            Item item = (Item) itemCombo.getSelectedItem();
            moveItemInstruction.setItemExpr(item.getId());
        }
        else
        {
            moveItemInstruction.setItemExpr(itemExprText.getText());
        }

        // set loc expr
        moveItemInstruction.setItemExpr(itemExprText.getText());
        moveItemInstruction.setToCollectionExpr(toCollectionExprText.getText());
    }

    @Override
    protected void validateData() throws DataNotValidException
    {
        if(itemRadio.isSelected() && itemCombo.getSelectedItem() == null)
        {
            throw new DataNotValidException("Не выбран предмет.", itemCombo);
        }
        if(itemExprRadio.isSelected() && "".equals(itemExprText.getText().trim()))
        {
            throw new DataNotValidException("Не введено выражение для предмета.", itemExprText);
        }
        if("".equals(toCollectionExprText.getText().trim()))
        {
            throw new DataNotValidException("Не введено выражение для коллекции.", toCollectionExprText);
        }
    }
}
