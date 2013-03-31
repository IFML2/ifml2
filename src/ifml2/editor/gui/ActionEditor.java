package ifml2.editor.gui;

import ifml2.editor.IFML2EditorException;
import ifml2.om.Action;
import ifml2.om.Procedure;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class ActionEditor extends AbstractEditor<Action>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JTextField descriptionText;
    private JComboBox procedureCallCombo;

    public ActionEditor(Window owner, @NotNull Action action, @NotNull HashMap<String, Procedure> procedures)
    {
        super(owner);
        initializeEditor("Действие", contentPane, buttonOK, buttonCancel);

        // init form data
        nameText.setText(action.getName());
        descriptionText.setText(action.getDescription());

        procedureCallCombo.setModel(new DefaultComboBoxModel(procedures.values().toArray()));

        //todo initialize other
    }

    @Override
    public void getData(@NotNull Action data) throws IFML2EditorException
    {
        //todo
    }
}
