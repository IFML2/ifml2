package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.GUIUtils;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.om.*;
import ifml2.vm.instructions.SetVarInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StoryOptionsEditor extends AbstractEditor<StoryOptions>
{
    private final static String STORY_OPTIONS_EDITOR_FORM_NAME = "Настройка истории";
    private final EventList<SetVarInstruction> varsClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox startLocCombo;
    private JComboBox startProcedureCombo;
    private JCheckBox showStartLocDescCheck;
    private JList varsList;
    private JButton addVarButton;
    private JButton editVarButton;
    private JButton delVarButton;
    private JTextField nameText;
    private JTextField authorText;
    private JTextField versionText;
    private JTextArea descriptionTextArea;

    public StoryOptionsEditor(Window owner, StoryOptions storyOptions, final Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(STORY_OPTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // clone data
        varsClone = GlazedLists.eventList(storyOptions.getVars());

        // set actions
        addVarButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SetVarInstruction setVarInstruction = new SetVarInstruction();
                if(EditorUtils.showAssociatedEditor(StoryOptionsEditor.this, setVarInstruction, storyDataHelper))
                {

                    varsClone.add(setVarInstruction);
                }
            }
        });
        editVarButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SetVarInstruction setVarInstruction = (SetVarInstruction) varsList.getSelectedValue();
                if (setVarInstruction != null)
                {
                    EditorUtils.showAssociatedEditor(StoryOptionsEditor.this, setVarInstruction, storyDataHelper);
                }
            }

            {
                setEnabled(false); // disabled at start
                varsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!varsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

        });
        delVarButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SetVarInstruction setVarInstruction = (SetVarInstruction) varsList.getSelectedValue();
                if (setVarInstruction != null &&
                    GUIUtils.showDeleteConfirmDialog(StoryOptionsEditor.this, "глобальную переменную", "глобальной переменной",
                            Word.GenderEnum.FEMININE))
                {
                    varsClone.remove(setVarInstruction);
                }
            }

            {
                setEnabled(false); // disabled at start
                varsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!varsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }


        });

        // -- init form --

        startProcedureCombo.setModel(new DefaultComboBoxModel(storyDataHelper.getProcedures().toArray()));
        startProcedureCombo.insertItemAt(null, 0);
        startProcedureCombo.setSelectedItem(storyOptions.getStartProcedureOption().getProcedure());

        startLocCombo.setModel(new DefaultComboBoxModel(storyDataHelper.getLocations().toArray()));
        startLocCombo.insertItemAt(null, 0);
        startLocCombo.setSelectedItem(storyOptions.getStartLocationOption().getLocation());

        showStartLocDescCheck.setSelected(storyOptions.getStartLocationOption().getShowStartLocDesc());

        varsList.setModel(new DefaultEventComboBoxModel<SetVarInstruction>(varsClone));

        StoryOptions.StoryDescription storyDescription = storyOptions.getStoryDescription();
        nameText.setText(storyDescription.getName());
        authorText.setText(storyDescription.getAuthor());
        versionText.setText(storyDescription.getVersion());
        descriptionTextArea.setText(storyDescription.getDescription());
    }

    @Override
    public void getData(@NotNull StoryOptions data)
    {
        StoryOptions.StartLocationOption startLocationOption = data.getStartLocationOption();
        startLocationOption.setLocation((Location) startLocCombo.getSelectedItem());
        startLocationOption.setShowStartLocDesc(showStartLocDescCheck.isSelected());

        data.getStartProcedureOption().setProcedure((Procedure) startProcedureCombo.getSelectedItem());

        data.setVars(varsClone);

        StoryOptions.StoryDescription storyDescription = data.getStoryDescription();
        storyDescription.setName(nameText.getText());
        storyDescription.setAuthor(authorText.getText());
        storyDescription.setVersion(versionText.getText());
        storyDescription.setDescription(descriptionTextArea.getText());
    }
}
