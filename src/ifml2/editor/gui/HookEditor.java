package ifml2.editor.gui;

import com.sun.istack.internal.NotNull;
import ifml2.GUIUtils;
import ifml2.om.Action;
import ifml2.om.*;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.HashMap;
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

    public HookEditor(@NotNull final Hook hook, @NotNull List<Action> actionList)
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
        // todo load parameters and current parameter after action select
        parameterCombo.setModel(new ComboBoxModel()           {

            // subscribe to action combo box changes
            {
                actionCombo.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Action selectedAction = (Action) actionCombo.getSelectedItem();
                        if(cachedAction != selectedAction)
                        {
                            System.out.println(MessageFormat.format("action changed to {0}", selectedAction));
                            cachedAction = selectedAction;
                            cacheParameters();
                            selectedParameter = cachedParameters.size() > 0 ? cachedParameters.values().iterator().next() : null;
                            repaint();
                        }
                    }
                });
            }

            Object selectedParameter = hook.getObjectElement();
            Action cachedAction = (Action) actionCombo.getSelectedItem();
            HashMap<String, Object> cachedParameters = new HashMap<String, Object>();

            private void cacheParameters()
            {
                cachedParameters.clear();
                if(cachedAction == null)
                {
                    return;
                }

                for(Template template : cachedAction.getTemplates())
                {
                    for(TemplateElement element : template.getElements())
                    {
                        if(element instanceof ObjectTemplateElement && element.parameter != null && !cachedParameters.containsKey(element.parameter.toLowerCase()))
                        {
                            cachedParameters.put(element.parameter.toLowerCase(), element.parameter);
                        }
                    }
                }
            }

            @Override
            public void setSelectedItem(Object anItem)
            {
                selectedParameter = anItem;
            }

            @Override
            public Object getSelectedItem()
            {
                return selectedParameter;
            }

            @Override
            public int getSize()
            {
                int size = cachedParameters.size();
                //System.out.println(MessageFormat.format("getSize() = {0}", size));
                return size;
            }

            @Override
            public Object getElementAt(int index)
            {
                Object element = cachedParameters.values().toArray()[index];
                //System.out.println(MessageFormat.format("getElementAt({0}) = {1}", index, element));
                return element;
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
        actionCombo.setModel(new DefaultComboBoxModel(actionList.toArray()));
        actionCombo.setSelectedItem(hook.getAction());
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
