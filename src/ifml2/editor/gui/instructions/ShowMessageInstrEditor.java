package ifml2.editor.gui.instructions;

import ifml2.GUIUtils;
import ifml2.vm.instructions.ShowMessageInstr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ShowMessageInstrEditor extends JDialog
{
    public static final String SHOWMESSAGE_INSTREDITOR_TITLE = "Вывести сообщение";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea messageText;
    private boolean isOk = false;

    public ShowMessageInstrEditor(Window owner, ShowMessageInstr instruction)
    {
        super(owner, SHOWMESSAGE_INSTREDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

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

        setData(instruction);
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

    public void setData(ShowMessageInstr data)
    {
        messageText.setText(data.getMessage());
    }

    public void getData(ShowMessageInstr data)
    {
        data.setMessage(messageText.getText());
    }

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
