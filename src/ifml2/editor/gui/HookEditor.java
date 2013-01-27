package ifml2.editor.gui;

import com.sun.istack.internal.NotNull;
import ifml2.GUIUtils;
import ifml2.om.Hook;
import ifml2.om.InstructionList;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.event.*;
import java.util.List;

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

    private static final String HOOK_EDITOR_TITLE = "Перехват";

    public HookEditor(@NotNull Hook hook, @NotNull List<ifml2.om.Action> actionList)
    {
        setTitle(HOOK_EDITOR_TITLE);
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

        // data clones - for underling objects (all plain data are edited just in controls)
        //todo instructionsClone = hook.instructionList.clone();
        
        // form init
        actionCombo.setModel(new DefaultComboBoxModel(actionList.toArray()));
        actionCombo.setSelectedItem(hook.getAction());
        // todo load parameters and current parameter after action select
        parameterCombo.setModel(new ComboBoxModel()
        {
            @Override
            public void setSelectedItem(Object anItem)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getSelectedItem()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getSize()
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getElementAt(int index)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addListDataListener(ListDataListener l)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeListDataListener(ListDataListener l)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
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
