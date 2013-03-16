package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.StoryOptions;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class StoryOptionsEditor extends AbstractEditor<StoryOptions>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox startLocCombo;
    private JComboBox startProcedureCombo;
    private JCheckBox showStartLocDescCheck;

    private final static String STORY_OPTIONS_EDITOR_FORM_NAME = "Настройка истории";

    public StoryOptionsEditor(Window owner, StoryOptions storyOptions, EventList<Location> locations, HashMap<String, Procedure> procedures)
    {
        super(owner);
        initializeEditor(STORY_OPTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // -- init form --

        startProcedureCombo.setModel(new DefaultComboBoxModel(procedures.values().toArray()));
        startProcedureCombo.insertItemAt(null, 0);
        startProcedureCombo.setSelectedItem(storyOptions.startProcedureOption.procedure);

        startLocCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        startLocCombo.setSelectedItem(storyOptions.startLocationOption.location);

        showStartLocDescCheck.setSelected(storyOptions.startLocationOption.showStartLocDesc);
    }

    @Override
    public void getData(@NotNull StoryOptions data)
    {
        data.startLocationOption.location = (Location) startLocCombo.getSelectedItem();
        data.startLocationOption.showStartLocDesc = showStartLocDescCheck.isSelected();
        data.startProcedureOption.procedure = (Procedure) startProcedureCombo.getSelectedItem();
    }
}
