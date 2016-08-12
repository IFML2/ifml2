package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.GUIUtils.EventComboBoxModelWithNullElement;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import ifml2.vm.instructions.SetVarInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static ifml2.om.Word.Gender.FEMININE;

public class StoryOptionsEditor extends AbstractEditor<StoryOptions>
{
    private final static String STORY_OPTIONS_EDITOR_FORM_NAME = "Настройка истории";
    private final EventList<SetVarInstruction> varsClone;
    private final Story.DataHelper storyDataHelper;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox startLocCombo;
    private JComboBox startProcedureCombo;
    private JCheckBox showStartLocDescCheck;
    private JTextField nameText;
    private JTextField authorText;
    private JTextField versionText;
    private JTextArea descriptionTextArea;
    private ListEditForm<SetVarInstruction> globalVarListEditForm;
    private JCheckBox disHelpCheck;
    private JCheckBox disDebugCheck;

    public StoryOptionsEditor(Window owner, StoryOptions storyOptions, final Story.DataHelper storyDataHelper)
    {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor(STORY_OPTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // clone data
        varsClone = GlazedLists.eventList(storyOptions.getVars());

        // -- init form --

        startProcedureCombo.setModel(new EventComboBoxModelWithNullElement<Procedure>(storyDataHelper.getProcedures(),
                storyOptions.getStartProcedureOption().getProcedure()));

        startLocCombo.setModel(new EventComboBoxModelWithNullElement<Location>(storyDataHelper.getLocations(),
                storyOptions.getStartLocationOption().getLocation()));

        showStartLocDescCheck.setSelected(storyOptions.getStartLocationOption().getShowStartLocDesc());

        // set vars
        globalVarListEditForm.bindData(varsClone);

        // set descr
        StoryOptions.StoryDescription storyDescription = storyOptions.getStoryDescription();
        nameText.setText(storyDescription.getName());
        authorText.setText(storyDescription.getAuthor());
        versionText.setText(storyDescription.getVersion());
        descriptionTextArea.setText(storyDescription.getDescription());

        // set disables
        StoryOptions.SystemCommandsDisableOption systemCommandsDisableOption = storyOptions.getSystemCommandsDisableOption();
        disHelpCheck.setSelected(systemCommandsDisableOption.isDisableHelp());
        disDebugCheck.setSelected(systemCommandsDisableOption.isDisableDebug());
    }

    @Override
    public void updateData(@NotNull StoryOptions data)
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

        StoryOptions.SystemCommandsDisableOption systemCommandsDisableOption = data.getSystemCommandsDisableOption();
        systemCommandsDisableOption.setDisableHelp(disHelpCheck.isSelected());
        systemCommandsDisableOption.setDisableDebug(disDebugCheck.isSelected());
    }

    private void createUIComponents()
    {
        globalVarListEditForm = new ListEditForm<SetVarInstruction>(this, "глобальную переменную", "глобальной переменной", FEMININE
        )
        {
            @Override
            protected SetVarInstruction createElement() throws Exception
            {
                SetVarInstruction setVarInstruction = new SetVarInstruction();
                return EditorUtils.showAssociatedEditor(StoryOptionsEditor.this, setVarInstruction, storyDataHelper) ? setVarInstruction
                                                                                                                     : null;

            }

            @Override
            protected boolean editElement(SetVarInstruction selectedElement) throws Exception
            {
                return EditorUtils.showAssociatedEditor(StoryOptionsEditor.this, selectedElement, storyDataHelper);
            }
        };
    }
}
