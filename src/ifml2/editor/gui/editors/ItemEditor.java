package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.DataNotValidException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.editor.gui.ListEditForm;
import ifml2.om.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

public class ItemEditor extends AbstractEditor<Item>
{
    private static final String ITEM_EDITOR_TITLE = "Предмет";
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
    private ListEditForm<Hook> hooksListEditForm;
    private ListEditForm<Role> rolesListEditForm;
    private boolean toGenerateId = false;
    private Item itemClone;
    private Story.DataHelper storyDataHelper;

    public ItemEditor(Window owner, @NotNull final Item item, final Story.DataHelper storyDataHelper)
    {
        super(owner);

        this.storyDataHelper = storyDataHelper;

        initializeEditor(ITEM_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // clone data
        try
        {
            this.itemClone = item.clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        bindData();

        // -- init form --

        editAttributesButton.setAction(new ButtonAction(editAttributesButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final EventList<Attribute> attributes = itemClone.getAttributes();
                ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(ItemEditor.this, attributes, storyDataHelper);
                if (objectAttributesEditor.showDialog())
                {
                    objectAttributesEditor.getData(attributes);
                }
            }
        });
        editWordsButton.setAction(new ButtonAction(editWordsButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final WordLinks wordLinks = itemClone.getWordLinks();
                WordLinksEditor wordLinksEditor = new WordLinksEditor(ItemEditor.this, storyDataHelper.getDictionary(), wordLinks);
                if (wordLinksEditor.showDialog())
                {
                    wordLinksEditor.getData(wordLinks);
                }
            }
        });

        // set common variables
        String id = itemClone.getId();
        toGenerateId = id == null || "".equals(id);

        // name and id generation listeners
        nameText.getDocument().addDocumentListener(new DocumentListener()
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
            { /* do nothing */ }
        });
        idText.addKeyListener(new KeyAdapter()
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
    }

    private void bindData()
    {
        // set id, name and description
        idText.setText(itemClone.getId());
        nameText.setText(itemClone.getName());
        descText.setText(itemClone.getDescription());

        // set WordLinks and subscribe to its changes
        final WordLinks wordLinks = itemClone.getWordLinks();
        wordsLabel.setText(wordLinks.getAllWords());
        wordLinks.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                wordsLabel.setText(((WordLinks) e.getSource()).getAllWords());
            }
        });

        // set item in inventory
        itemInInventoryCheck.setSelected(itemClone.getStartingPosition().getInventory());

        // set item in locations
        itemInLocationsList.setModel(new DefaultEventListModel<Location>(storyDataHelper.getLocations()));
        DefaultEventSelectionModel<Location> selectionModel = new DefaultEventSelectionModel<Location>(storyDataHelper.getLocations());
        itemInLocationsList.setSelectionModel(selectionModel);
        selectionModel.setValueIsAdjusting(true);
        try
        {
            for (Location startLocation : itemClone.getStartingPosition().getLocations())
            {
                int index = storyDataHelper.getLocations().indexOf(startLocation);
                selectionModel.addSelectionInterval(index, index);
            }
        }
        finally
        {
            selectionModel.setValueIsAdjusting(false);
        }
        itemInLocationsList.ensureIndexIsVisible(selectionModel.getAnchorSelectionIndex());

        // set attributes
        attributesList.setModel(new DefaultEventListModel<Attribute>(itemClone.getAttributes()));

        // set roles
        rolesListEditForm.bindData(itemClone.getRoles());

        // set hooks
        hooksListEditForm.bindData(itemClone.getHooks());
    }

    private boolean editHook(@Nullable Hook hook)
    {
        if (hook != null)
        {
            try
            {
                HookEditor hookEditor = new HookEditor(ItemEditor.this, hook, true, storyDataHelper);
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
        }
        return false;
    }

    private void updateId()
    {
        if (toGenerateId)
        {
            idText.setText(storyDataHelper.generateIdByName(nameText.getText(), Item.class));
        }
    }

    @Override
    protected void validateData() throws DataNotValidException
    {
        //check name
        if (nameText.getText().trim().length() == 0)
        {
            throw new DataNotValidException("У предмета должно быть задано имя.", nameText);
        }

        // check dictionary
        if (itemClone.getWordLinks().getWords().size() == 0)
        {
            throw new DataNotValidException("У предмета не задан словарь.", editWordsButton);
        }

        // check id
        String id = idText.getText().trim();
        if (id.length() == 0)
        {
            throw new DataNotValidException("У предмета должен быть задан идентификатор.", idText);
        }

        Object object = storyDataHelper.findObjectById(id);
        if (object != null && !object.equals(itemClone))
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
                    "У предмета должен быть уникальный идентификатор - не пересекаться с другими локациями, предметами и словарём.\n" +
                    MessageFormat.format("Найден другой объект с тем же идентификатором: \"{0}\" типа \"{1}\".", object, className),
                    idText);
        }
    }

    @Override
    public void getData(@NotNull Item item)
    {
        item.setId(idText.getText().trim());
        item.setName(nameText.getText().trim());
        item.setDescription(descText.getText());

        item.setWordLinks(itemClone.getWordLinks());

        item.getStartingPosition().setInventory(itemInInventoryCheck.isSelected());
        EventList<Location> locations = item.getStartingPosition().getLocations();
        locations.clear();
        for (Object object : itemInLocationsList.getSelectedValues())
        {
            locations.add((Location) object);
        }

        item.setAttributes(itemClone.getAttributes());
        item.setHooks(GlazedLists.eventList(itemClone.getHooks())); // rewrite data in EventList
    }

    private void createUIComponents()
    {
        rolesListEditForm = new ListEditForm<Role>(this, "роль", "роли", Word.GenderEnum.FEMININE, Role.class)
        {
            @Override
            protected Role createElement() throws Exception
            {
                return null; //todo create role
            }

            @Override
            protected boolean editElement(Role selectedElement) throws Exception
            {
                return false; //todo edit role
            }
        };

        hooksListEditForm = new ListEditForm<Hook>(this, "перехват", "перехвата", Word.GenderEnum.MASCULINE, Hook.class)
        {
            @Override
            protected Hook createElement() throws Exception
            {
                Hook hook = new Hook();
                return editHook(hook) ? hook : null;
            }

            @Override
            protected boolean editElement(Hook selectedElement) throws Exception
            {
                return selectedElement != null && editHook(selectedElement);
            }
        };
    }
}
