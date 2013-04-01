package ifml2.editor.gui;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Action;
import ifml2.om.Procedure;
import ifml2.om.Template;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ActionEditor extends AbstractEditor<Action>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JTextField descriptionText;
    private JComboBox procedureCallCombo;
    private JList templatesList;
    private JButton addTemplateButton;
    private JButton editTemplateButton;
    private JButton delTemplateButton;

    public ActionEditor(Window owner, @NotNull Action action, @NotNull HashMap<String, Procedure> procedures)
    {
        super(owner);
        initializeEditor("Действие", contentPane, buttonOK, buttonCancel);

        // init actions and listeners
        addTemplateButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //todo
            }
        });
        editTemplateButton.setAction(new AbstractAction("Изменить...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                templatesList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!templatesList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                //todo
            }
        });
        delTemplateButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                templatesList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!templatesList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                //todo
            }
        });

        // init form data
        nameText.setText(action.getName());
        descriptionText.setText(action.getDescription());
        templatesList.setModel(new DefaultEventListModel<Template>(action.templates));
        procedureCallCombo.setModel(new DefaultComboBoxModel(procedures.values().toArray()));

        //todo initialize other
    }

    @Override
    public void getData(@NotNull Action data) throws IFML2EditorException
    {
        //todo
    }
}
