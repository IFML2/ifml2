package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.DataNotValidException;
import ifml2.om.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class LocationEditor extends AbstractEditor<Location>
{
    private static final String LOCATION_EDITOR_TITLE = "Локация";
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
    private JButton addHookButton;
    private JButton editHookButton;
    private JButton deleteHookButton;
    private JList hooksList;
    private boolean toGenerateId = false;
    private ArrayList<Item> itemsClone = null;
    private EventList<Attribute> attributesClone = null;
    private EventList<Hook> hooksClone = null;
    private Location location;
    private Story.DataHelper storyDataHelper;

    public LocationEditor(Window owner, Location location, final Story.DataHelper storyDataHelper)
    {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor(LOCATION_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        this.location = location;

        addItemButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = new Item();
                if (editItem(item))
                {
                    itemsClone.add(item);
                    updateItems();
                    itemsList.setSelectedValue(item, true);
                }
            }
        });
        editItemButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                itemsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!itemsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editItem((Item) itemsList.getSelectedValue());
            }
        });
        delItemButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                itemsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!itemsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = (Item) itemsList.getSelectedValue();
                if (item != null)
                {
                    int answer = JOptionPane
                            .showConfirmDialog(LocationEditor.this, "Вы уверены, что хотите удалить этот предмет?");
                    if (answer == 0)
                    {
                        itemsClone.remove(item);
                        updateItems();
                    }
                }
            }
        });
        editAttributesButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(LocationEditor.this,
                                                                                           attributesClone,
                                                                                           storyDataHelper);
                if (objectAttributesEditor.showDialog())
                {
                    updateAttributes();
                }
            }
        });

        // hooks
        addHookButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Hook hook = new Hook();
                if (editHook(hook))
                {
                    hooksClone.add(hook);
                }
            }
        });
        editHookButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                setEnabled(false); // initially disabled
                hooksList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!hooksList.isSelectionEmpty()); // dependent from selection
                    }
                });
            }

            @Override()
            public void actionPerformed(ActionEvent e)
            {
                Hook selectedHook = (Hook) hooksList.getSelectedValue();
                if (selectedHook != null)
                {
                    editHook(selectedHook);
                }
            }
        });
        deleteHookButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                setEnabled(false); // initially disabled
                hooksList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!hooksList.isSelectionEmpty()); // dependent from selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Hook selectedHook = (Hook) hooksList.getSelectedValue();
                if (selectedHook != null && JOptionPane.showConfirmDialog(LocationEditor.this,
                                                                          "Вы действительно хотите удалить выбранный перехват?",
                                                                          "Удаление перехвата",
                                                                          JOptionPane.YES_NO_OPTION,
                                                                          JOptionPane.QUESTION_MESSAGE) ==
                                            JOptionPane.YES_OPTION)
                {
                    hooksClone.remove(selectedHook);
                }
            }
        });

        itemsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Item item = (Item) itemsList.getSelectedValue();
                    if (item != null)
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
                if (e.getKeyChar() != '\0')
                {
                    toGenerateId = false;
                }
            }
        });

        updateDictionaryLinks(storyDataHelper.getDictionary());
        //TODO:dictWordCombo.setSelectedItem(location.getWord());

        updateLocationLinks(storyDataHelper.getLocations());
        northCombo.setSelectedItem(location.getNorth());
        eastCombo.setSelectedItem(location.getEast());
        southCombo.setSelectedItem(location.getSouth());
        westCombo.setSelectedItem(location.getWest());

        itemsClone = new ArrayList<Item>(location.getItems());
        updateItems();

        attributesClone = GlazedLists.eventList(location.getAttributes());
        updateAttributes();

        locationNameText.setText(location.getName());
        locationIDText.setText(location.getId());
        descriptionText.setText(location.getDescription());

        String id = location.getId();
        toGenerateId = id == null || "".equals(id);

        // set hooks
        hooksClone = GlazedLists.eventList(location.hooks);
        hooksList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
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
    }

    private boolean editHook(Hook hook)
    {
        try
        {
            HookEditor hookEditor = new HookEditor(LocationEditor.this, hook, false, storyDataHelper);
            if (hookEditor.showDialog())
            {
                hookEditor.getData(hook);
                return true;
            }
        }
        catch (IFML2Exception e)
        {
            GUIUtils.showErrorMessage(this, e);
        }
        return false;
    }

    private void updateId()
    {
        if (toGenerateId)
        {
            locationIDText.setText(storyDataHelper.generateIdByName(locationNameText.getText(), Location.class));
        }
    }

    private void updateAttributes()
    {
        attributesList.setListData(attributesClone.toArray());
    }

    private void updateItems()
    {
        DefaultListModel itemsListModel = new DefaultListModel();
        for (Item item : itemsClone)
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

    private boolean editItem(Item item)
    {
        if (item != null)
        {
            ItemEditor itemEditor = new ItemEditor(LocationEditor.this, item, storyDataHelper);
            if (itemEditor.showDialog())
            {
                itemEditor.getData(item);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void validateData() throws DataNotValidException
    {
        // check name
        if (locationNameText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("У локации должно быть задано имя.", locationNameText);
        }

        // check id
        String id = locationIDText.getText().trim();
        if (id.length() == 0)
        {
            throw new DataNotValidException("У локации должен быть задан идентификатор.", locationIDText);
        }

        Object object = storyDataHelper.findObjectById(id);
        if (object != null && !object.equals(location))
        {
            String className = null;
            try
            {
                className = storyDataHelper.getObjectClassName(object);
            }
            catch (IFML2Exception e)
            {
                GUIUtils.showErrorMessage(this, e);
            }
            throw new DataNotValidException(
                    "У локации должен быть уникальный идентификатор - не пересекаться с другими локациями, предметами и словарём.\n" +
                    MessageFormat.format("Найден другой объект с тем же идентификатором: \"{0}\" типа \"{1}\".", object,
                                         className), locationIDText);
        }
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

        location.setName(locationNameText.getText().trim());
        location.setId(locationIDText.getText().trim());
        location.setDescription(descriptionText.getText());
        location.hooks = GlazedLists.eventList(hooksClone); // rewrite data in EventList
    }


    //region Item in location actions
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
    //endregion
}
