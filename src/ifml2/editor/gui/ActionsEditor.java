package ifml2.editor.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Action;
import ifml2.om.Procedure;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JList libsActionsList;

    private static final String ACTIONS_EDITOR_FORM_NAME = "Действия";

    private final EventList<Action> actionsClone;

    private HashMap<String, Procedure> procedures;

    public ActionsEditor(Window owner, @NotNull EventList<Action> actions, @NotNull final HashMap<String, Procedure> procedures, Story story)
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
                if(editAction(action))
                {
                    actionsClone.add(action);
                    actionsList.setSelectedValue(action, true);
                }
            }
        });
        editButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                actionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!actionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Action action = (Action) actionsList.getSelectedValue();
                if(action != null)
                {
                    editAction(action);
                }
            }
        });
        delButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                actionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!actionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

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
        });

        // listeners
        actionsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    Action action = (Action) actionsList.getSelectedValue();
                    if(action != null)
                    {
                        editAction(action);
                    }
                }
            }
        });

        // clone data
        actionsClone = new BasicEventList<Action>();
        for(Action action : actions)
        {
            actionsClone.add(action);
        }

        // load data
        actionsList.setModel(new DefaultEventListModel<Action>(actionsClone));
        libsActionsList.setModel(new DefaultEventListModel<Action>(story.getAllActions()));
    }

    private boolean editAction(@NotNull Action action)
    {
        ActionEditor actionEditor = new ActionEditor(this, action, procedures);
        if(actionEditor.showDialog())
        {
            try
            {
                actionEditor.getData(action);
                return true;
            }
            catch (IFML2EditorException ex)
            {
                GUIUtils.showErrorMessage(this, ex);
            }
        }
        return false;
    }

    @Override
    public void getData(@NotNull EventList<Action> data) throws IFML2EditorException
    {
        data.clear();
        data.addAll(actionsClone);
    }
}
