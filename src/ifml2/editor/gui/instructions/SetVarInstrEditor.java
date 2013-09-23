package ifml2.editor.gui.instructions;

import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.SetVarInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetVarInstrEditor extends AbstractInstrEditor
{
    private static final String SET_VAR_EDITOR_TITLE = "Установить переменную";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JTextField valueText;
    private JLabel typeLabel;

    public SetVarInstrEditor(Window owner, SetVarInstruction instruction)
    {
        super(owner);
        initializeEditor(SET_VAR_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        nameText.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                typeLabel.setText("test");
            }
        });

        // -- init form data --

        nameText.setText(instruction.getName());
        valueText.setText(instruction.getValue());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass()
    {
        return SetVarInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException
    {
        getData(instruction);

        SetVarInstruction setVarInstruction = (SetVarInstruction) instruction;
        setVarInstruction.setName(nameText.getText().trim());
        setVarInstruction.setValue(valueText.getText().trim());
    }

    @Override
    protected void validateData() throws DataNotValidException
    {
        // check name
        if(nameText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("Должно быть задано имя переменной.", nameText);
        }

        // check value
        if(valueText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("Должно быть задано значение переменной.", valueText);
        }
    }
}
