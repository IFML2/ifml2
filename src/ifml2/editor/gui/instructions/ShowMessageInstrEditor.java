package ifml2.editor.gui.instructions;

import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.ShowMessageInstr;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

import static ifml2.vm.instructions.ShowMessageInstr.MessageTypeEnum.EXPRESSION;
import static ifml2.vm.instructions.ShowMessageInstr.MessageTypeEnum.TEXT;

public class ShowMessageInstrEditor extends AbstractInstrEditor
{
    private static final String SHOW_MESSAGE_INSTR_EDITOR_TITLE = ShowMessageInstr.getTitle();
    private static final String TYPE_ERROR = "Message type \"{0}\" is unknown";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea messageText;
    private JRadioButton textTypeRadio;
    private JRadioButton exprTypeRadio;
    private JCheckBox beginWithCapCheck;
    private JCheckBox carriageReturnCheck;

    public ShowMessageInstrEditor(Window owner, @NotNull ShowMessageInstr instruction) throws IFML2EditorException
    {
        super(owner);
        initializeEditor(SHOW_MESSAGE_INSTR_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- init form data --

        switch (instruction.getType())
        {
            case TEXT:
                textTypeRadio.setSelected(true);
                break;
            case EXPRESSION:
                exprTypeRadio.setSelected(true);
                break;
            default:
                throw new IFML2EditorException(MessageFormat.format(TYPE_ERROR, instruction.getType()));
        }

        messageText.setText(instruction.getMessage());

        beginWithCapCheck.setSelected(instruction.getBeginWithCap());
        carriageReturnCheck.setSelected(instruction.getCarriageReturn());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass()
    {
        return ShowMessageInstr.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException
    {
        getData(instruction);

        ShowMessageInstr showMessageInstr = (ShowMessageInstr) instruction;

        if (textTypeRadio.isSelected())
        {
            showMessageInstr.setType(TEXT);
        }
        else if (exprTypeRadio.isSelected())
        {
            showMessageInstr.setType(EXPRESSION);
        }
        else
        {
            throw new IFML2EditorException("No type is selected!");
        }

        showMessageInstr.setMessage(messageText.getText());

        showMessageInstr.setBeginWithCap(beginWithCapCheck.isSelected());
        showMessageInstr.setCarriageReturn(carriageReturnCheck.isSelected());
    }
}
