package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.om.Hook;
import ifml2.om.InstructionList;

import javax.swing.*;
import java.awt.event.*;

public class HookEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox actionCombo;
    private JComboBox parameterCombo;
    private JRadioButton доДействияRadioButton;
    private JRadioButton вместоДействияRadioButton;
    private JRadioButton послеДействияRadioButton;
    private JButton editInstructionsButton;
    private InstructionList instructionsClone;

    public HookEditor(Hook hook)
    {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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

        // form actions init
        EditInstructionsAction editInstructionsAction = new EditInstructionsAction();
        editInstructionsButton.setAction(editInstructionsAction);

        // data clones
        //todo instructionsClone = hook.instructionList.clone();
        
        // form init
        // todo load actions and current action
        // todo load parameters and current parameter after action select
        // todo set radio
    }

    private void onOK()
    {
        dispose();
    }

    private void onCancel()
    {
        dispose();
    }

    private class EditInstructionsAction extends AbstractAction
    {
        public EditInstructionsAction()
        {
            super("Редактировать инструкции");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            //todo InstructionsEditor instructionsEditor = new InstructionsEditor();
        }
    }
}
