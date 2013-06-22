package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.om.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ItemEditor extends AbstractEditor<Item>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField idText;
    private JTextField nameText;
    private JTextArea descText;
    private JButton editAttributesButton;
    private JList attributesList;
    private JButton editWordsButton;
    private JLabel wordsLabel;
    private JCheckBox itemInInventoryCheck;
    private JList itemInLocationsList;
    private JList hooksList;
    private JButton addHookButton;
    private JButton editHookButton;
    private JButton deleteHookButton;

    private static final String ITEM_EDITOR_TITLE = "Предмет";

    private Story story = null;
    private boolean toGenerateId = false;

    // clones
    private EventList<Attribute> attributesClone = null;
    private WordLinks wordLinksClone = null;
    private EventList<Hook> hooksClone = null;

    // updated actions
    private final AbstractAction editHookAction = new AbstractAction("Изменить...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override()
        public void actionPerformed(ActionEvent e)
        {
            Hook selectedHook = (Hook) hooksList.getSelectedValue();
            if (selectedHook != null)
            {
                editHook(selectedHook);
            }
        }
    };
    private final AbstractAction deleteHookAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Hook selectedHook = (Hook) hooksList.getSelectedValue();
            if (selectedHook != null && JOptionPane.showConfirmDialog(ItemEditor.this, "Вы действительно хотите удалить выбранный перехват?",
                    "Удаление перехвата", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
            {
                hooksClone.remove(selectedHook);
            }
        }
    };

    public ItemEditor(Window owner, @NotNull final Story story, @NotNull final Item item)
    {
        super(owner);
        initializeEditor(ITEM_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- init form --

        editAttributesButton.setAction(new AbstractAction("Изменить...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(ItemEditor.this, attributesClone, ItemEditor.this.story);
                if(objectAttributesEditor.showDialog())
                {
                    objectAttributesEditor.getData(attributesClone);
                }
            }
        });
        editWordsButton.setAction(new AbstractAction("Изменить...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                WordLinksEditor wordLinksEditor = new WordLinksEditor(ItemEditor.this, story.getDictionary(), wordLinksClone);
                if(wordLinksEditor.showDialog())
                {
                    wordLinksEditor.getData(wordLinksClone);
                }
            }
        });
        
        // hooks
        addHookButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Hook hook = new Hook();
                    HookEditor hookEditor = new HookEditor(ItemEditor.this, hook, ItemEditor.this.story.getAllActions());
                    if(hookEditor.showDialog())
                    {
                        hooksClone.add(hook);
                    }
                }
                catch (IFML2Exception ex)
                {
                    GUIUtils.showErrorMessage(ItemEditor.this, ex);
                }
            }
        });
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
            GUIUtils.showErrorMessage(this, e);
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
        itemInInventoryCheck.setSelected(item.getStartingPosition().getInventory());
        // set item in locations
        itemInLocationsList.setModel(new DefaultEventListModel<Location>(story.getLocations()));
        DefaultEventSelectionModel<Location> selectionModel = new DefaultEventSelectionModel<Location>(story.getLocations());
        itemInLocationsList.setSelectionModel(selectionModel);
        selectionModel.setValueIsAdjusting(true);
        try
        {
            for(Location startLocation : item.getStartingPosition().getLocations())
            {
                int index = story.getLocations().indexOf(startLocation);
                selectionModel.addSelectionInterval(index, index);
            }
        }
        finally
        {
           selectionModel.setValueIsAdjusting(false);
        }
        itemInLocationsList.ensureIndexIsVisible(selectionModel.getAnchorSelectionIndex());

        // set attributes
        attributesClone = GlazedLists.eventList(item.getAttributes());
        attributesList.setModel(new DefaultEventListModel<Attribute>(attributesClone));
        
        // set hooks
        hooksClone = GlazedLists.eventList(item.hooks);
        hooksList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    Hook selectedHook = (Hook) hooksList.getSelectedValue();
                    if (selectedHook != null)
                    {
                        editHook(selectedHook);
                    }
                }
            }
        });
        hooksList.setModel(new DefaultEventListModel<Hook>(hooksClone));

        // initially update form actions
        UpdateHookActions();
    }

    private void editHook(Hook hook)
    {
        try
        {
            HookEditor hookEditor = new HookEditor(ItemEditor.this, hook, story.getAllActions());
            if(hookEditor.showDialog())
            {
                hookEditor.getData(hook);
            }
        }
        catch (IFML2Exception ex)
        {
            GUIUtils.showErrorMessage(this, ex);
        }
    }

    private void UpdateHookActions()
    {
        boolean hookIsSelected = hooksList.getSelectedValue() != null;
        editHookAction.setEnabled(hookIsSelected);
        deleteHookAction.setEnabled(hookIsSelected);
    }

    @Override
    public void getData(@NotNull Item item)
    {
        item.setId(idText.getText());
        item.setName(nameText.getText());
        item.setDescription(descText.getText());

        item.setWordLinks(wordLinksClone);

        item.getStartingPosition().setInventory(itemInInventoryCheck.isSelected());
        EventList<Location> locations = item.getStartingPosition().getLocations();
        locations.clear();
        for(Object object : itemInLocationsList.getSelectedValues())
        {
            locations.add((Location) object);
        }

        item.setAttributes(attributesClone);
        item.hooks = hooksClone; // rewrite data in EventList
    }

    private void updateId()
    {
        if(toGenerateId)
        {
            idText.setText(story.generateIdByName(nameText.getText(), Item.class));
        }
    }
}
