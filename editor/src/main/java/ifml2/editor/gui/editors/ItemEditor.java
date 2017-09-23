package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.DataNotValidException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Attribute;
import ifml2.om.Hook;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Role;
import ifml2.om.RoleDefinition;
import ifml2.om.Story;
import ifml2.om.WordLinks;
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
import java.util.List;

import static ifml2.om.Word.Gender.FEMININE;
import static ifml2.om.Word.Gender.MASCULINE;

public class ItemEditor extends AbstractEditor<Item> {
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
    private Item originalItem;

    public ItemEditor(Window owner, @NotNull final Item item, final Story.DataHelper storyDataHelper) {
        super(owner);

        this.originalItem = item;
        this.storyDataHelper = storyDataHelper;

        $$$setupUI$$$();
        initializeEditor(ITEM_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // clone data
        try {
            this.itemClone = item.clone();
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }

        bindData();

        // -- init form --

        editAttributesButton.setAction(new ButtonAction(editAttributesButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final EventList<Attribute> attributes = itemClone.getAttributes();
                ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(ItemEditor.this, attributes, storyDataHelper);
                if (objectAttributesEditor.showDialog()) {
                    objectAttributesEditor.updateData(attributes);
                }
            }
        });
        editWordsButton.setAction(new ButtonAction(editWordsButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final WordLinks wordLinks = itemClone.getWordLinks();
                WordLinksEditor wordLinksEditor = new WordLinksEditor(ItemEditor.this, storyDataHelper.getDictionary(), wordLinks);
                if (wordLinksEditor.showDialog()) {
                    wordLinksEditor.updateData(wordLinks);
                }
            }
        });

        // set common variables
        String id = itemClone.getId();
        toGenerateId = id == null || "".equals(id);

        // name and id generation listeners
        nameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateId();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateId();
            }

            @Override
            public void changedUpdate(DocumentEvent e) { /* do nothing */ }
        });
        idText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() != '\0') {
                    toGenerateId = false;
                }
            }
        });
    }

    private void bindData() {
        // set id, name and description
        idText.setText(itemClone.getId());
        nameText.setText(itemClone.getName());
        descText.setText(itemClone.getDescription());

        // set WordLinks and subscribe to its changes
        final WordLinks wordLinks = itemClone.getWordLinks();
        wordsLabel.setText(wordLinks.getAllWords());
        wordLinks.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
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
        try {
            for (Location startLocation : itemClone.getStartingPosition().getLocations()) {
                int index = storyDataHelper.getLocations().indexOf(startLocation);
                selectionModel.addSelectionInterval(index, index);
            }
        } finally {
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

    private boolean editHook(@Nullable Hook hook) {
        if (hook != null) {
            try {
                HookEditor hookEditor = new HookEditor(ItemEditor.this, hook, true, storyDataHelper);
                if (hookEditor.showDialog()) {
                    hookEditor.updateData(hook);
                    return true;
                }
            } catch (IFML2Exception e) {
                GUIUtils.showErrorMessage(this, e);
            }
        }
        return false;
    }

    private void updateId() {
        if (toGenerateId) {
            idText.setText(storyDataHelper.generateIdByName(nameText.getText(), Item.class));
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        //check name
        if (nameText.getText().trim().length() == 0) {
            throw new DataNotValidException("У предмета должно быть задано имя.", nameText);
        }

        // check dictionary
        if (itemClone.getWordLinks().getWords().size() == 0) {
            throw new DataNotValidException("У предмета не задан словарь.", editWordsButton);
        }

        // check id
        String id = idText.getText().trim();
        if (id.length() == 0) {
            throw new DataNotValidException("У предмета должен быть задан идентификатор.", idText);
        }

        Object object = storyDataHelper.findObjectById(id);
        if (object != null && !object.equals(originalItem)) {
            String className = null;
            try {
                className = storyDataHelper.getObjectClassName(object);
            } catch (IFML2Exception e) {
                GUIUtils.showErrorMessage(this, e);
            }
            throw new DataNotValidException(
                    "У предмета должен быть уникальный идентификатор - не пересекаться с другими локациями, предметами и словарём.\n" +
                            MessageFormat.format("Найден другой объект с тем же идентификатором: \"{0}\" типа \"{1}\".", object, className),
                    idText);
        }
    }

    @Override
    public void updateData(@NotNull Item item) {
        try {
            // update clone's local data
            itemClone.setId(idText.getText().trim());
            itemClone.setName(nameText.getText().trim());
            itemClone.setDescription(descText.getText());
            itemClone.getStartingPosition().setInventory(itemInInventoryCheck.isSelected());
            EventList<Location> locations = itemClone.getStartingPosition().getLocations();
            locations.clear();
            for (Object object : itemInLocationsList.getSelectedValues()) {
                locations.add((Location) object);
            }

            // copy clone to orig obj
            itemClone.copyTo(item);
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }
    }

    private void createUIComponents() {
        rolesListEditForm = new ListEditForm<Role>(this, "роль", "роли", FEMININE, Role.class) {
            @Override
            protected Role createElement() throws Exception {
                final List<RoleDefinition> allRoleDefinitions = storyDataHelper.getCopyOfAllRoleDefinitions();

                // remove all definitions that already used
                for (Role role : itemClone.getRoles()) {
                    allRoleDefinitions.remove(role.getRoleDefinition());
                }

                if (allRoleDefinitions.size() == 0) {
                    JOptionPane.showMessageDialog(ItemEditor.this, "Уже добавлены все возможные роли", "Нет больше ролей",
                            JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }

                final RoleDefinition roleDefinition = (RoleDefinition) JOptionPane
                        .showInputDialog(ItemEditor.this, "Выберите роль", "Добавление роли", JOptionPane.QUESTION_MESSAGE, null,
                                allRoleDefinitions.toArray(), null);

                if (roleDefinition != null) {
                    Role role = new Role(roleDefinition);
                    return editRole(role) ? role : null;
                }

                return null;
            }

            @Override
            protected boolean editElement(Role selectedElement) throws Exception {
                return editRole(selectedElement);
            }
        };

        hooksListEditForm = new ListEditForm<Hook>(this, "перехват", "перехвата", MASCULINE, Hook.class) {
            @Override
            protected Hook createElement() throws Exception {
                Hook hook = new Hook();
                return editHook(hook) ? hook : null;
            }

            @Override
            protected boolean editElement(Hook selectedElement) throws Exception {
                return editHook(selectedElement);
            }
        };
    }

    private boolean editRole(@Nullable Role role) {
        if (role != null) {
            try {
                RoleEditor roleEditor = new RoleEditor(ItemEditor.this, role, originalItem, storyDataHelper);
                if (roleEditor.showDialog()) {
                    roleEditor.updateData(role);
                    return true;
                }
            } catch (IFML2Exception e) {
                GUIUtils.showErrorMessage(this, e);
            }
        }
        return false;
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(450, 255));
        contentPane.setPreferredSize(new Dimension(640, 480));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonCancel.setMnemonic('C');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPane.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Основные свойства", panel3);
        idText = new JTextField();
        panel3.add(idText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Идентификатор:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameText = new JTextField();
        panel3.add(nameText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Название:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Описание:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(58, 128), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(0, 128), null, 0, false));
        descText = new JTextArea();
        descText.setLineWrap(true);
        descText.setWrapStyleWord(true);
        scrollPane1.setViewportView(descText);
        final JLabel label4 = new JLabel();
        label4.setText("Словарь:");
        panel3.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("<html>Первоначальное<br/>\nположение<br/>\n<i>(держите Ctrl<br/>\nдля выделения<br/>\nнескольких)</i>:</html>");
        panel3.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        itemInInventoryCheck = new JCheckBox();
        itemInInventoryCheck.setText("В инвентаре");
        itemInInventoryCheck.setMnemonic('В');
        itemInInventoryCheck.setDisplayedMnemonicIndex(0);
        panel4.add(itemInInventoryCheck, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel4.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setBorder(BorderFactory.createTitledBorder("В локациях:"));
        itemInLocationsList = new JList();
        scrollPane2.setViewportView(itemInLocationsList);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel5, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        wordsLabel = new JLabel();
        wordsLabel.setText("");
        panel5.add(wordsLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editWordsButton = new JButton();
        editWordsButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Edit24.gif")));
        editWordsButton.setText("Изменить...");
        editWordsButton.setMnemonic('М');
        editWordsButton.setDisplayedMnemonicIndex(2);
        panel5.add(editWordsButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 1, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Признаки предмета", panel6);
        final JScrollPane scrollPane3 = new JScrollPane();
        panel6.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(307, 38), null, 0, false));
        attributesList = new JList();
        scrollPane3.setViewportView(attributesList);
        editAttributesButton = new JButton();
        editAttributesButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Edit24.gif")));
        editAttributesButton.setText("Изменить...");
        editAttributesButton.setMnemonic('З');
        editAttributesButton.setDisplayedMnemonicIndex(1);
        panel6.add(editAttributesButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(97, 38), null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Роли", panel7);
        panel7.add(rolesListEditForm.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Перехваты действий", panel8);
        panel8.add(hooksListEditForm.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label1.setLabelFor(idText);
        label2.setLabelFor(nameText);
        label3.setLabelFor(descText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
