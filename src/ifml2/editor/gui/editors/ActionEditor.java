package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Action;
import ifml2.om.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

public class ActionEditor extends AbstractEditor<Action>
{
    private final EventList<Template> templatesClone;
    private final EventList<Restriction> restrictionsClone;
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
    private JButton upRestrictionButton;
    private JButton downRestrictionButton;
    private JButton addRestrictionButton;
    private JButton editRestrictionButton;
    private JButton delRestrictionButton;
    private Story.DataHelper storyDataHelper;

    public ActionEditor(Window owner, @NotNull Action action, Story.DataHelper storyDataHelper)
    {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor("Действие", contentPane, buttonOK, buttonCancel);

        // init actions and listeners
        addTemplateButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Template template = new Template();
                if (editTemplate(template))
                {
                    templatesClone.add(template);
                    templatesList.setSelectedValue(template, true);
                }
            }
        });
        editTemplateButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
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
                if (template != null)
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
                if (JOptionPane.showConfirmDialog(ActionEditor.this, "Вы действительно хотите удалить этот шаблон?",
                                                  "Удаление шаблона", JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    Template selectedAction = (Template) templatesList.getSelectedValue();
                    templatesClone.remove(selectedAction);
                }
            }
        });
        addRestrictionButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Restriction restriction = new Restriction();
                if (editRestriction(restriction))
                {
                    restrictionsClone.add(restriction);
                    restrictionsList.setSelectedValue(restriction, true);
                }
            }
        });
        editRestrictionButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!restrictionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Restriction restriction = (Restriction) restrictionsList.getSelectedValue();
                if (restriction != null)
                {
                    editRestriction(restriction);
                }
            }
        });
        delRestrictionButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!restrictionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Restriction restriction = (Restriction) restrictionsList.getSelectedValue();
                if (restriction != null &&
                    GUIUtils.showDeleteConfirmDialog(ActionEditor.this, "ограничение", "ограничения",
                                                     Word.GenderEnum.NEUTER))
                {
                    restrictionsClone.remove(restriction);
                }
            }
        });
        upRestrictionButton.setAction(new AbstractAction("", GUIUtils.UP_ICON)
        {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(restrictionsList.getSelectedIndex() > 0); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selIdx = restrictionsList.getSelectedIndex();
                if (selIdx > 0)
                {
                    Collections.swap(restrictionsClone, selIdx, selIdx - 1);
                    restrictionsList.setSelectedIndex(selIdx - 1);
                }
            }
        });
        downRestrictionButton.setAction(new AbstractAction("", GUIUtils.DOWN_ICON)
        {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(restrictionsList.getSelectedIndex() <
                                   restrictionsList.getModel().getSize() - 1); // depends on selection and list length
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selIdx = restrictionsList.getSelectedIndex();
                if (selIdx < restrictionsClone.size() - 1)
                {
                    Collections.swap(restrictionsClone, selIdx, selIdx + 1);
                    restrictionsList.setSelectedIndex(selIdx + 1);
                }
            }
        });

        // listeners
        templatesList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Template template = (Template) templatesList.getSelectedValue();
                    if (template != null)
                    {
                        editTemplate(template);
                    }
                }
            }
        });
        restrictionsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Restriction restriction = (Restriction) restrictionsList.getSelectedValue();
                    if (restriction != null)
                    {
                        editRestriction(restriction);
                    }
                }
            }
        });

        // clone data
        templatesClone = GlazedLists.eventList(action.getTemplates()); //todo is that really clones members???
        restrictionsClone = GlazedLists.eventList(action.getRestrictions()); //todo is that really clones members???

        // init form data
        nameText.setText(action.getName());
        descriptionText.setText(action.getDescription());
        templatesList.setModel(new DefaultEventListModel<Template>(templatesClone));
        procedureCallCombo.setModel(new DefaultComboBoxModel(storyDataHelper.getProcedures().toArray()));
        procedureCallCombo.setSelectedItem(action.getProcedureCall().getProcedure());
        restrictionsList.setModel(new DefaultEventListModel<Restriction>(restrictionsClone));
    }

    private boolean editRestriction(Restriction restriction)
    {
        RestrictionEditor restrictionEditor = new RestrictionEditor(this, restriction, storyDataHelper);
        if (restrictionEditor.showDialog())
        {
            try
            {
                restrictionEditor.getData(restriction);
                return true;
            }
            catch (IFML2EditorException e)
            {
                GUIUtils.showErrorMessage(this, e);
            }
        }
        return false;
    }

    private boolean editTemplate(@NotNull Template template)
    {
        Procedure selectedProcedure = (Procedure) procedureCallCombo.getSelectedItem();
        TemplateEditor templateEditor = new TemplateEditor(this, template, selectedProcedure);
        if (templateEditor.showDialog())
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

        data.setRestrictions(restrictionsClone);
    }

    @Override
    protected void validateData() throws DataNotValidException
    {
        if (nameText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("У действия должно быть имя!", nameText);
        }
    }
}
