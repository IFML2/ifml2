package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.om.InstructionList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InstructionsEditor extends JDialog
{
    private static final String INSTR_EDITOR_TITLE = "Инструкции";

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList instructionsList;
    private boolean isOk;

    public InstructionsEditor(Window owner, InstructionList instructionList)
    {
        super(owner, INSTR_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

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

        // init form
        //todo instructionsList.setModel();
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

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }

    public void getData(InstructionList instructionList)
    {
        //todo To change body of created methods use File | Settings | File Templates.
    }
}
