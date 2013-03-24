package ifml2.editor.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Action;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ActionsEditor extends AbstractEditor<Action>
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
    private final AbstractAction editAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            //todo action
        }
    };
    private final AbstractAction delAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            //todo action
        }
    };

    public ActionsEditor(Frame owner, EventList<Action> actions)
    {
        super(owner);
        initializeEditor(ACTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // set actions
        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //todo action
            }
        });
        editButton.setAction(editAction);
        delButton.setAction(delAction);

        // clone data
        actionsClone = new BasicEventList<Action>();
        for(Action action : actions)
        {
            actionsClone.add(action);
        }

        // todo form init
        actionsList.setModel(new DefaultEventListModel<Action>(actionsClone));
    }

    @Override
    public void getData(@NotNull Action data) throws IFML2EditorException
    {
        //todo get actions data from editor
    }
}
