package ifml2.editor.gui;

import com.sun.istack.internal.NotNull;
import ifml2.GUIUtils;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.InstructionList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.List;

public class HookEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox actionCombo;
    private JComboBox parameterCombo;
    private JRadioButton beforeRadio;
    private JRadioButton insteadRadio;
    private JRadioButton afterRadio;
    private JButton editInstructionsButton;
    private InstructionList instructionsClone;

    private static final String HOOK_EDITOR_TITLE = "Перехват";

    public HookEditor(Window owner, @NotNull final Hook hook, @NotNull List<Action> actionList)
    {
        super(owner, HOOK_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        // -- dialog init --

        setContentPane(contentPane);
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

        // -- form actions init --

        EditInstructionsAction editInstructionsAction = new EditInstructionsAction();
        editInstructionsButton.setAction(editInstructionsAction);

        // -- data init --

        // data clones - for underling objects (all plain data are edited just in controls)
        try
        {
            instructionsClone = hook.getInstructionList().clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError("InstructionList isn't cloneable!");
        }

        //  -- form init --
        // load parameters and current parameter after action select
        actionCombo.addActionListener(new ActionListener()
        {
            Action prevSelectedAction;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Action selectedAction = (Action) actionCombo.getSelectedItem();
                if (prevSelectedAction != selectedAction)
                {
                    prevSelectedAction = selectedAction;
                    parameterCombo.setModel(new DefaultComboBoxModel(selectedAction.getAllParameters()));
                    if(parameterCombo.getItemCount() > 0) // if there are elements ...
                    {
                        parameterCombo.setSelectedIndex(0); // ... select first element
                    }
                }
                hook.setAction(selectedAction);
            }
        });
        actionCombo.setModel(new DefaultComboBoxModel(actionList.toArray()));
        if(hook.getAction() != null)
        {
            actionCombo.setSelectedItem(hook.getAction()); // select hook's action
        }
        else if (actionCombo.getItemCount() > 0) // if hook's action is null (for new hook e.g.) ...
        {
            actionCombo.setSelectedIndex(0); // ... select first
        }

        // set radio
        switch (hook.getType())
        {
            case BEFORE:
                beforeRadio.setSelected(true);
                break;
            case AFTER:
                afterRadio.setSelected(true);
                break;
            case INSTEAD:
                insteadRadio.setSelected(true);
                break;
            default:
                throw new InternalError(MessageFormat.format("Unknown hook type: {0}", hook.getType()));
        }
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
            InstructionsEditor instructionsEditor = new InstructionsEditor(HookEditor.this, instructionsClone);
            if(instructionsEditor.showDialog())
            {
                instructionsEditor.getData(instructionsClone);
            }
        }
    }
}
