package ifml2.editor.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Action;
import ifml2.om.Procedure;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ActionsEditor extends AbstractEditor<EventList<Action>>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList actionsList;
    private JButton addButton;
    private JButton delButton;
    private JButton editButton;

    private static final String ACTIONS_EDITOR_FORM_NAME = "Действия";

    private final EventList<Action> actionsClone;

    private HashMap<String, Procedure> procedures;

    private final AbstractAction editAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Action action = (Action) actionsList.getSelectedValue();
            if(action != null)
            {
                ActionEditor actionEditor = new ActionEditor(ActionsEditor.this, action, procedures);
                if(actionEditor.showDialog())
                {
                    try
                    {
                        actionEditor.getData(action);
                    }
                    catch (IFML2EditorException ex)
                    {
                        GUIUtils.showErrorMessage(ActionsEditor.this, ex);
                    }
                }
            }
        }
    };
    private final AbstractAction delAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if(JOptionPane.showConfirmDialog(ActionsEditor.this, "Вы действительно хотите удалить это действие?",
                    "Удаление действия", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
            {
                Action selectedAction = (Action) actionsList.getSelectedValue();
                actionsClone.remove(selectedAction);
            }
        }
    };

    public ActionsEditor(Window owner, @NotNull EventList<Action> actions, @NotNull final HashMap<String, Procedure> procedures)
    {
        super(owner);
        initializeEditor(ACTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        this.procedures = procedures;

        // set actions
        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Action action = new Action();
                ActionEditor actionEditor = new ActionEditor(ActionsEditor.this, action, procedures);
                if(actionEditor.showDialog())
                {
                    try
                    {
                        actionEditor.getData(action);
                        actionsClone.add(action);
                        actionsList.setSelectedValue(action, true);
                    }
                    catch (IFML2EditorException ex)
                    {
                        GUIUtils.showErrorMessage(ActionsEditor.this, ex);
                    }
                }
            }
        });
        editButton.setAction(editAction);
        delButton.setAction(delAction);

        // set listeners
        actionsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                UpdateActions();
            }
        });

        // clone data
        actionsClone = new BasicEventList<Action>();
        for(Action action : actions)
        {
            actionsClone.add(action);
        }

        actionsList.setModel(new DefaultEventListModel<Action>(actionsClone));
        //todo load actions from libs

        UpdateActions();
    }

    private void UpdateActions()
    {
        boolean isActionSelected = !actionsList.isSelectionEmpty();
        editAction.setEnabled(isActionSelected);
        delAction.setEnabled(isActionSelected);
    }

    @Override
    public void getData(@NotNull EventList<Action> actions) throws IFML2EditorException
    {
        actions.clear();
        actions.addAll(actionsClone);
    }
}
