package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.GUIUtils;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.StoryOptions;
import ifml2.vm.instructions.SetVarInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class StoryOptionsEditor extends AbstractEditor<StoryOptions>
{
    private final static String STORY_OPTIONS_EDITOR_FORM_NAME = "Настройка истории";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox startLocCombo;
    private JComboBox startProcedureCombo;
    private JCheckBox showStartLocDescCheck;
    private JList varsList;
    private JButton addVarButton;
    private JButton editVarButton;
    private JButton delVarbutton;

    public StoryOptionsEditor(Window owner, StoryOptions storyOptions, EventList<Location> locations, HashMap<String, Procedure> procedures)
    {
        super(owner);
        initializeEditor(STORY_OPTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // set actions
        addVarButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

            }
        });

        // -- init form --

        startProcedureCombo.setModel(new DefaultComboBoxModel(procedures.values().toArray()));
        startProcedureCombo.insertItemAt(null, 0);
        startProcedureCombo.setSelectedItem(storyOptions.getStartProcedureOption().getProcedure());

        startLocCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        startLocCombo.setSelectedItem(storyOptions.getStartLocationOption().getLocation());

        showStartLocDescCheck.setSelected(storyOptions.getStartLocationOption().getShowStartLocDesc());

        varsList.setModel(new DefaultEventComboBoxModel<SetVarInstruction>(storyOptions.getVars()));
    }

    @Override
    public void getData(@NotNull StoryOptions data)
    {
        data.getStartLocationOption().setLocation((Location) startLocCombo.getSelectedItem());
        data.getStartLocationOption().setShowStartLocDesc(showStartLocDescCheck.isSelected());
        data.getStartProcedureOption().setProcedure((Procedure) startProcedureCombo.getSelectedItem());
    }
}
