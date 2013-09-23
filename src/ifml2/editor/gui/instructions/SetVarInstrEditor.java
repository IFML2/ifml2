package ifml2.editor.gui.instructions;

import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.vm.RunningContext;
import ifml2.vm.Variable;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.SetVarInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SetVarInstrEditor extends AbstractInstrEditor
{
    private static final String SET_VAR_EDITOR_TITLE = "Установить переменную";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JTextField valueText;
    private JLabel typeLabel;
    private RunningContext runningContext;

    public SetVarInstrEditor(Window owner, SetVarInstruction instruction, RunningContext runningContext) //todo нет поддержки конструкторов с 3мя параметрами!
    {
        super(owner);
        initializeEditor(SET_VAR_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        this.runningContext = runningContext;

        nameText.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updateScope();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updateScope();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                /* do nothing */
            }
        });

        // -- init form data --

        nameText.setText(instruction.getName());
        valueText.setText(instruction.getValue());
    }

    private void updateScope()
    {
        Variable.VariableScope variableScope = runningContext.getVariableScopeByName(nameText.getText().trim());
        typeLabel.setText(variableScope != null ? variableScope.toString() : "локальная");
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
        if (nameText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("Должно быть задано имя переменной.", nameText);
        }

        // check value
        if (valueText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("Должно быть задано значение переменной.", valueText);
        }
    }
}
