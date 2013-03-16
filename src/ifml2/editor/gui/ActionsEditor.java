package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Action;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ActionsEditor extends AbstractEditor<Action>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private static final String ACTIONS_EDITOR_FORM_NAME = "Действия";

    public ActionsEditor(Frame owner, EventList<Action> actions)
    {
        super(owner);
        initializeEditor(ACTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // todo form init
    }

    @Override
    public void getData(@NotNull Action data) throws IFML2EditorException
    {
        //todo get actions data from editor
    }
}
