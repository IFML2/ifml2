package ifml2.editor.gui;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.InstructionList;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

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
    private JList instructionsList;

    // data to edit
    private InstructionList instructionListClone; // no need to clone - InstructionList isn't modified here

    private static final String HOOK_EDITOR_TITLE = "Перехват";
    private boolean isOk;

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

        editInstructionsButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionsEditor instructionsEditor = new InstructionsEditor(HookEditor.this, instructionListClone);
                if(instructionsEditor.showDialog())
                {
                    instructionsEditor.getData(instructionListClone);
                }
            }
        });

        // -- data init --
        try
        {
            instructionListClone = hook.getInstructionList().clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
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
                    if (parameterCombo.getItemCount() > 0) // if there are elements ...
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
            if(hook.getObjectElement() != null) // if hook has assigned objectElement
            {
                parameterCombo.setSelectedItem(hook.getObjectElement());
            }
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

        instructionsList.setModel(new DefaultEventListModel<Instruction>(instructionListClone.getInstructions()));
        instructionsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    Instruction instruction = (Instruction) instructionsList.getSelectedValue();
                    if (instruction != null)
                    {
                        EditorUtils.showAssociatedEditor(HookEditor.this, instruction);
                    }
                }
            }
        });
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

    public void getData(@NotNull Hook hook)
    {
        hook.setAction((Action) actionCombo.getSelectedItem());
        hook.setObjectElement((String) parameterCombo.getSelectedItem());
        if(beforeRadio.isSelected())
        {
            hook.setType(Hook.HookTypeEnum.BEFORE);
        }
        else if(afterRadio.isSelected())
        {
            hook.setType(Hook.HookTypeEnum.AFTER);
        }
        else if(insteadRadio.isSelected())
        {
            hook.setType(Hook.HookTypeEnum.INSTEAD);
        }
        else
        {
            throw new InternalError("No hook type selected!");
        }
    }
}
