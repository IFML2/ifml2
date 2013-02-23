package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.GUIUtils;
import ifml2.om.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationEditor extends JDialog
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

    private final EditItemAction editItemAction = new EditItemAction();
    private final DelItemAction delItemAction = new DelItemAction();

    private static final String LOCATION_EDITOR_FORM_NAME = "Локация";

    private boolean isOk = false;
    private boolean toGenerateId = false;

    private ArrayList<Item> itemsClone = null;
    private EventList<Attribute> attributesClone = null;

    //private DelItemAction delItemAction = new DelItemAction();
    private Story story = null;

    public LocationEditor(Frame owner)
    {
        super(owner, LOCATION_EDITOR_FORM_NAME, ModalityType.DOCUMENT_MODAL);

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

        //AddItemAction addItemAction = new AddItemAction();
        NewItemAction newItemAction = new NewItemAction();
        addItemButton.setAction(newItemAction);
        editItemButton.setAction(editItemAction);
        delItemButton.setAction(delItemAction);
        EditAttributesAction editAttributesAction = new EditAttributesAction();
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

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }

    public void setAllData(Story story, Location location)
    {
        updateDictionaryLinks(story.getDictionary());
        //TODO:dictWordCombo.setSelectedItem(location.getWord());

        updateLocationLinks(story.getLocations());
        northCombo.setSelectedItem(location.getNorth());
        eastCombo.setSelectedItem(location.east);
        southCombo.setSelectedItem(location.south);
        westCombo.setSelectedItem(location.west);

        itemsClone = new ArrayList<Item>(location.getItems());
        updateItems();

        attributesClone = GlazedLists.eventList(location.getAttributes());
        updateAttributes();

        setData(location);

        this.story = story;

        String id = location.getId();
        toGenerateId = id == null || "".equals(id);
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

    public void getAllData(Location location)
    {
        getData(location);

        //TODO:location.setWord((Word) dictWordCombo.getSelectedItem());

        location.setNorth((Location) northCombo.getSelectedItem());
        location.east = (Location) eastCombo.getSelectedItem();
        location.south = (Location) southCombo.getSelectedItem();
        location.west = (Location) westCombo.getSelectedItem();

        location.setItems(itemsClone);
        location.setAttributes(attributesClone);
    }

    void setData(Location data)
    {
        locationNameText.setText(data.getName());
        locationIDText.setText(data.getId());
        descriptionText.setText(data.getDescription());
    }

    void getData(Location data)
    {
        data.setName(locationNameText.getText());
        data.setId(locationIDText.getText());
        data.setDescription(descriptionText.getText());
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
            super("Удалить...");
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

    private class NewItemAction extends AbstractAction
    {
        private NewItemAction()
        {
            super("Добавить...");
        }

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
    }

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

    private class EditItemAction extends AbstractAction
    {
        private EditItemAction()
        {
            super("Редактировать...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            editItem((Item) itemsList.getSelectedValue());
        }
    }

    private class DelItemAction extends AbstractAction
    {
        private DelItemAction()
        {
            super("Удалить...");
        }

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
    }

    private class EditAttributesAction extends AbstractAction
    {
        private EditAttributesAction()
        {
            super("Изменить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(LocationEditor.this, attributesClone, story);
            if(objectAttributesEditor.showDialog())
            {
                updateAttributes();
            }
        }
    }
}
