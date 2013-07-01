package ifml2.editor.gui;

import ifml2.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ActionsEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private static final String ACTIONS_EDITOR_FORM_NAME = "Действия";

    public ActionsEditor(Frame owner)
    {
        super(owner, ACTIONS_EDITOR_FORM_NAME, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        buttonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onCancel();}
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

        // todo form init
    }

    private void onOK()
    {
        // add your code here
        dispose();
    }

    private void onCancel()
    {
        // add your code here if necessary
        dispose();
    }

    public boolean showDialog()
    {
        setVisible(true);
        return false;
    }
}
