package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.GUIUtils;
import ifml2.om.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationEditor extends AbstractEditor<Location>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField locationIDText;
    private JTextField locationNameText;
    private JTextArea descriptionText;
    private JComboBox dictWordCombo;
    private JComboBox northCombo;
    private JComboBox eastCombo;
    private JComboBox southCombo;
    private JComboBox westCombo;
    private JList itemsList;
    private JButton addItemButton;
    private JButton delItemButton;
    private JButton editItemButton;
    private JButton editAttributesButton;
    private JList attributesList;

    private final AbstractAction editItemAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            editItem((Item) itemsList.getSelectedValue());
        }
    };
    private final AbstractAction delItemAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = (Item) itemsList.getSelectedValue();
            if(item != null)
            {
                int answer = JOptionPane.showConfirmDialog(LocationEditor.this, "Вы уверены, что хотите удалить этот предмет?");
                if(answer == 0)
                {
                    itemsClone.remove(item);
                    updateItems();
                }
            }
        }
    };

    private static final String LOCATION_EDITOR_TITLE = "Локация";

    private boolean toGenerateId = false;

    private ArrayList<Item> itemsClone = null;
    private EventList<Attribute> attributesClone = null;

    private Story story = null;

    public LocationEditor(Window owner, final Story story, Location location)
    {
        super(owner);
        initializeEditor(LOCATION_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        addItemButton.setAction(new AbstractAction("Добавить...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = new Item();
                if(editItem(item))
                {
                    itemsClone.add(item);
                    updateItems();
                    itemsList.setSelectedValue(item, true);
                }
            }
        });
        editItemButton.setAction(editItemAction);
        delItemButton.setAction(delItemAction);
        AbstractAction editAttributesAction = new AbstractAction("Изменить...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(LocationEditor.this, attributesClone, story);
                if(objectAttributesEditor.showDialog())
                {
                    updateAttributes();
                }
            }
        };
        editAttributesButton.setAction(editAttributesAction);

        itemsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateActions();
            }
        });

        itemsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    Item item = (Item) itemsList.getSelectedValue();
                    if(item != null)
                    {
                        editItem(item);
                    }
                }
            }
        });

        locationNameText.getDocument().addDocumentListener(new DocumentListener()
        {
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
            public void changedUpdate(DocumentEvent e)
            {
                //do nothing
            }
        });

        locationIDText.addKeyListener(new KeyAdapter()
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

        updateActions();

        updateDictionaryLinks(story.getDictionary());
        //TODO:dictWordCombo.setSelectedItem(location.getWord());

        updateLocationLinks(story.getLocations());
        northCombo.setSelectedItem(location.getNorth());
        eastCombo.setSelectedItem(location.getEast());
        southCombo.setSelectedItem(location.getSouth());
        westCombo.setSelectedItem(location.getWest());

        itemsClone = new ArrayList<Item>(location.getItems());
        updateItems();

        attributesClone = GlazedLists.eventList(location.getAttributes());
        updateAttributes();

        setData(location);

        this.story = story;

        String id = location.getId();
        toGenerateId = id == null || "".equals(id);
    }

    private void updateId()
    {
        if(toGenerateId)
        {
            locationIDText.setText(story.generateIdByName(locationNameText.getText()));
        }
    }

    private void updateActions()
    {
        boolean itemIsSelected = itemsList.getSelectedValue() != null;
        editItemAction.setEnabled(itemIsSelected);
        delItemAction.setEnabled(itemIsSelected);
    }

    private void updateAttributes()
    {
        attributesList.setListData(attributesClone.toArray());
    }

    private void updateItems()
    {
        DefaultListModel itemsListModel = new DefaultListModel();
        for(Item item : itemsClone)
        {
            itemsListModel.addElement(item);
        }
        itemsList.setModel(itemsListModel);
    }

    private void updateLocationLinks(EventList<Location> locations)
    {
        northCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        northCombo.insertItemAt(null, 0);

        eastCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        eastCombo.insertItemAt(null, 0);

        southCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        southCombo.insertItemAt(null, 0);

        westCombo.setModel(new DefaultComboBoxModel(locations.toArray()));
        westCombo.insertItemAt(null, 0);
    }

    private void updateDictionaryLinks(HashMap<String, Word> dictionary)
    {
        dictWordCombo.setModel(new DefaultComboBoxModel(dictionary.values().toArray()));
        dictWordCombo.insertItemAt(null, 0);
    }

    void setData(Location data)
    {
        locationNameText.setText(data.getName());
        locationIDText.setText(data.getId());
        descriptionText.setText(data.getDescription());
    }

    @Override
    public void getData(@NotNull Location location)
    {
        location.setNorth((Location) northCombo.getSelectedItem());
        location.setEast((Location) eastCombo.getSelectedItem());
        location.setSouth((Location) southCombo.getSelectedItem());
        location.setWest((Location) westCombo.getSelectedItem());

        location.setItems(itemsClone);
        location.setAttributes(attributesClone);

        location.setName(locationNameText.getText());
        location.setId(locationIDText.getText());
        location.setDescription(descriptionText.getText());
    }

    /*
    private class AddItemAction extends AbstractAction
    {
        private AddItemAction()
        {
            super("Добавить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = (Item) JOptionPane.showInputDialog(dialog, "Выберите добавляемый предмет", "Добавить предмет",
                    JOptionPane.QUESTION_MESSAGE, null, story.items.values().toArray(), null);

            if(item != null)
            {
                itemsClone.add(item);
                updateItems();
                itemsList.setSelectedValue(item, true);
            }
        }
    }

    private class DelItemAction extends AbstractAction
    {
        private DelItemAction()
        {
            super("Удалить");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = (Item) itemsList.getSelectedValue();
            if(item != null)
            {
                int answer = JOptionPane.showConfirmDialog(dialog, "Вы уверены, что хотите удалить этот предмет из локации?");
                if(answer == 0)
                {
                    itemsClone.remove(item);
                    updateItems();
                }
            }
        }
    }
    */

    private boolean editItem(Item item)
    {
        if(item != null)
        {
            ItemEditor itemEditor = new ItemEditor(LocationEditor.this, story, item);
            if(itemEditor.showDialog())
            {
                itemEditor.getData(item);
                return true;
            }
        }

        return false;
    }
}
