package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import com.sun.istack.internal.NotNull;
import ifml2.GUIUtils;
import ifml2.om.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

public class ItemEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField idText;
    private JTextField nameText;
    private JTextArea descText;
    private JButton editAttributesButton;
    private JList attributesList;
    private JComboBox locCombo;
    private JButton editWordsButton;
    private JLabel wordsLabel;
    private JCheckBox itemInInventoryCheck;
    private JList itemInLocationsList;
    private JList hooksList;
    private JButton addHookButton;
    private JButton editHookButton;
    private JButton deleteHookButton;

    private static final String ITEM_EDITOR_TITLE = "Предмет";

    boolean isOk = false;
    private Story story = null;
    private boolean toGenerateId = false;
    // clones
    private EventList<Attribute> attributesClone = null;
    private WordLinks wordLinksClone = null;
    private EventList<Hook> hooksClone = null;
    private final EditHookAction editHookAction = new EditHookAction();
    private final DeleteHookAction deleteHookAction = new DeleteHookAction();

    public ItemEditor(@NotNull final Story story, @NotNull final Item item)
    {
        // window tuning
        setTitle(ITEM_EDITOR_TITLE);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);
        
        // OK/Cancel buttons
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

        EditAttributesAction editAttributesAction = new EditAttributesAction();
        editAttributesButton.setAction(editAttributesAction);
        EditWordsAction editWordsAction = new EditWordsAction();
        editWordsButton.setAction(editWordsAction);
        
        // hooks
        AddHookAction addHookAction = new AddHookAction();
        addHookButton.setAction(addHookAction);
        editHookButton.setAction(editHookAction);
        deleteHookButton.setAction(deleteHookAction);
        hooksList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                UpdateHookActions();
            }
        });
        
        // --- set data with listeners ---

        // set common variables
        this.story = story;
        String id = item.getId();
        toGenerateId = id == null || "".equals(id);

        // set id, name and description
        idText.setText(item.getId());
        nameText.setText(item.getName());
        descText.setText(item.getDescription());
        // name and id generation listeners
        nameText.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updateId();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updateId();
            }

            @Override
            public void changedUpdate(DocumentEvent e) { /* do nothing */ }
        });
        idText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar() != '\0')
                {
                    toGenerateId = false;
                }
            }
        });

        // set dictionary
        try
        {
            wordLinksClone = item.getWordLinks().clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError("WordLinks isn't clonable!");
        }
        wordsLabel.setText(wordLinksClone.getAllWords());
        wordLinksClone.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                wordsLabel.setText(((WordLinks)e.getSource()).getAllWords());
            }
        });

        // set item in inventory
        itemInInventoryCheck.setSelected(item.startingPosition.inventory);
        // set item in locations
        itemInLocationsList.setModel(new DefaultEventListModel<Location>(story.getLocations()));
        DefaultEventSelectionModel<Location> selectionModel = new DefaultEventSelectionModel<Location>(story.getLocations());
        itemInLocationsList.setSelectionModel(selectionModel);
        selectionModel.setValueIsAdjusting(true);
        try
        {
            for(Location startLocation : item.startingPosition.locations)
            {
                int index = story.getLocations().indexOf(startLocation);
                selectionModel.addSelectionInterval(index, index);
            }
        }
        finally
        {
           selectionModel.setValueIsAdjusting(false);
        }

        // set attributes
        attributesClone = GlazedLists.eventList(item.getAttributes());
        attributesList.setModel(new DefaultEventListModel<Attribute>(attributesClone));
        
        // set hooks
        hooksClone = GlazedLists.eventList(item.hooks);
        hooksList.setModel(new DefaultEventListModel<Hook>(hooksClone));
        UpdateHookActions();
    }

    private void UpdateHookActions()
    {
        boolean hookIsSelected = hooksList.getSelectedValue() != null;
        editHookAction.setEnabled(hookIsSelected);
        deleteHookAction.setEnabled(hookIsSelected);
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

    public void getData(Item item)
    {
        item.setId(idText.getText());
        item.setName(nameText.getText());
        item.setDescription(descText.getText());

        item.setWordLinks(wordLinksClone);

        item.startingPosition.inventory = itemInInventoryCheck.isSelected();
        item.startingPosition.locations.clear();
        for(Object object : itemInLocationsList.getSelectedValues())
        {
            item.startingPosition.locations.add((Location)object);
        }

        item.setAttributes(attributesClone);
        item.hooks = hooksClone;
    }

    private void updateId()
    {
        if(toGenerateId)
        {
            idText.setText(story.generateIdByName(nameText.getText()));
        }
    }

    private class EditAttributesAction extends AbstractAction
    {
        private EditAttributesAction()
        {
            super("Изменить");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(attributesClone, story);
            objectAttributesEditor.setVisible(true);
            if(objectAttributesEditor.isOk)
            {
                objectAttributesEditor.getData(attributesClone);
            }
        }
    }

    private class EditWordsAction extends AbstractAction
    {
        private EditWordsAction()
        {
            super("Изменить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            WordLinksEditor wordLinksEditor = new WordLinksEditor();
            wordLinksEditor.setAllData(story.getDictionary(), wordLinksClone);
            wordLinksEditor.setVisible(true);
            if(wordLinksEditor.isOk)
            {
                wordLinksEditor.getData(wordLinksClone);
            }
        }
    }

    private class AddHookAction extends AbstractAction
    {
        AddHookAction()
        {
            super("Добавить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            HookEditor hookEditor = new HookEditor(new Hook());
            hookEditor.setVisible(true);
            // todo analyze isOk
        }
    }

    private class EditHookAction extends AbstractAction
    {
        private EditHookAction()
        {
            super("Изменить...");
        }

        @Override()
        public void actionPerformed(ActionEvent e)
        {
            //todo
        }
    }

    private class DeleteHookAction extends AbstractAction
    {
        private DeleteHookAction()
        {
            super("Удалить");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            //todo
        }
    }
}
