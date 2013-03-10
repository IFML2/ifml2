package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.GUIUtils;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.StoryOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class StoryOptionsEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox startLocCombo;
    private JComboBox startProcedureCombo;
    private JCheckBox showStartLocDescCheck;
    private boolean isOk = false;
    private final static String STORY_OPTIONS_EDITOR_FORM_NAME = "Настройка истории";

    public StoryOptionsEditor(Window owner)
    {
        super(owner, STORY_OPTIONS_EDITOR_FORM_NAME, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK()
    {
        isOk = true;
        dispose();
    }

    private void onCancel()
    {
        isOk = false;
        dispose();
    }

    public void setAllData(StoryOptions storyOptions, EventList<Location> locations, HashMap<String, Procedure> procedures)
    {
        startProcedureCombo.setModel(new DefaultComboBoxModel(procedures.values().toArray()));
        startProcedureCombo.insertItemAt(null, 0);
        startProcedureCombo.setSelectedItem(storyOptions.startProcedureOption.procedure);

        startLocCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        startLocCombo.setSelectedItem(storyOptions.startLocationOption.location);

        showStartLocDescCheck.setSelected(storyOptions.startLocationOption.showStartLocDesc);
    }

    public void getAllData(StoryOptions storyOptions)
    {
        storyOptions.startLocationOption.location = (Location) startLocCombo.getSelectedItem();
        storyOptions.startLocationOption.showStartLocDesc = showStartLocDescCheck.isSelected();
        storyOptions.startProcedureOption.procedure = (Procedure) startProcedureCombo.getSelectedItem();
    }

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
