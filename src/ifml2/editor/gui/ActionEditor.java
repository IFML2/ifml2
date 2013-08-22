package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JList restrictionsList;
    private JButton upButton;
    private JButton downButton;

    private final EventList<Template> templatesClone;

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
                Template template = new Template();
                if(editTemplate(template))
                {
                    templatesClone.add(template);
                    templatesList.setSelectedValue(template, true);
                }
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
                Template template = (Template) templatesList.getSelectedValue();
                if(template != null)
                {
                    editTemplate(template);
                }
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
                if(JOptionPane.showConfirmDialog(ActionEditor.this, "Вы действительно хотите удалить этот шаблон?",
                        "Удаление шаблона", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    Template selectedAction = (Template) templatesList.getSelectedValue();
                    templatesClone.remove(selectedAction);
                }
            }
        });

        // listeners
        templatesList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    Template template = (Template) templatesList.getSelectedValue();
                    if(template != null)
                    {
                        editTemplate(template);
                    }
                }
            }
        });

        // clone data
        templatesClone = GlazedLists.eventList(action.getTemplates());

        // init form data
        nameText.setText(action.getName());
        descriptionText.setText(action.getDescription());
        templatesList.setModel(new DefaultEventListModel<Template>(templatesClone));
        procedureCallCombo.setModel(new DefaultComboBoxModel(procedures.values().toArray()));
        procedureCallCombo.setSelectedItem(action.getProcedureCall().getProcedure());

        //todo initialize other
    }

    private boolean editTemplate(@NotNull Template template)
    {
        Procedure selectedProcedure = (Procedure) procedureCallCombo.getSelectedItem();
        TemplateEditor templateEditor = new TemplateEditor(this, template, selectedProcedure);
        if(templateEditor.showDialog())
        {
            try
            {
                templateEditor.getData(template);
                return true;
            }
            catch (IFML2EditorException e)
            {
                GUIUtils.showErrorMessage(this, e);
            }
        }
        return false;
    }

    @Override
    public void getData(@NotNull Action data) throws IFML2EditorException
    {
        data.setName(nameText.getText());
        data.setDescription(descriptionText.getText());

        EventList<Template> templates = data.getTemplates();
        templates.clear();
        templates.addAll(templatesClone);

        data.getProcedureCall().setProcedure((Procedure) procedureCallCombo.getSelectedItem());
    }
}
