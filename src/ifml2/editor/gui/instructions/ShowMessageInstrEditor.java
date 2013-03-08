package ifml2.editor.gui.instructions;

import ifml2.GUIUtils;
import ifml2.vm.instructions.ShowMessageInstr;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;

import static ifml2.vm.instructions.ShowMessageInstr.MessageTypeEnum.EXPRESSION;
import static ifml2.vm.instructions.ShowMessageInstr.MessageTypeEnum.TEXT;

public class ShowMessageInstrEditor extends JDialog
{
    private static final String SHOW_MESSAGE_INSTR_EDITOR_TITLE = "Вывести сообщение";
    private static final String TYPE_ERROR = "Message type \"{0}\" is unknown";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea messageText;
    private JRadioButton textTypeRadio;
    private JRadioButton exprTypeRadio;
    private JCheckBox beginWithCapCheck;
    private JCheckBox carriageReturnCheck;
    private boolean isOk = false;

    public ShowMessageInstrEditor(Window owner, @NotNull ShowMessageInstr instruction)
    {
        super(owner, SHOW_MESSAGE_INSTR_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // -- init form --
        switch (instruction.getType())
        {
            case TEXT:
                textTypeRadio.setSelected(true);
                break;
            case EXPRESSION:
                exprTypeRadio.setSelected(true);
                break;
            default:
                throw new InternalError(MessageFormat.format(TYPE_ERROR, instruction.getType()));
        }

        messageText.setText(instruction.getMessage());

        beginWithCapCheck.setSelected(instruction.getBeginWithCap());
        carriageReturnCheck.setSelected(instruction.getCarriageReturn());
    }

    private void onOK()
    {
        isOk = true;
        dispose();
    }

    private void onCancel()
    {
        isOk = false;
        dispose();
    }

    public void getData(ShowMessageInstr data)
    {
        if(textTypeRadio.isSelected())
        {
            data.setType(TEXT);
        }
        else if(exprTypeRadio.isSelected())
        {
            data.setType(EXPRESSION);
        }
        else
        {
            throw new InternalError("No type is selected!");
        }

        data.setMessage(messageText.getText());

        data.setBeginWithCap(beginWithCapCheck.isSelected());
        data.setCarriageReturn(carriageReturnCheck.isSelected());
    }

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
