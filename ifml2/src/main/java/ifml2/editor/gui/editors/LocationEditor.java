package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.DataNotValidException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Attribute;
import ifml2.om.Hook;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Story;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ifml2.om.Location.ExitDirection;
import static ifml2.om.Location.ExitDirection.DOWN;
import static ifml2.om.Location.ExitDirection.EAST;
import static ifml2.om.Location.ExitDirection.NORTH;
import static ifml2.om.Location.ExitDirection.NORTH_EAST;
import static ifml2.om.Location.ExitDirection.NORTH_WEST;
import static ifml2.om.Location.ExitDirection.SOUTH;
import static ifml2.om.Location.ExitDirection.SOUTH_EAST;
import static ifml2.om.Location.ExitDirection.SOUTH_WEST;
import static ifml2.om.Location.ExitDirection.UP;
import static ifml2.om.Location.ExitDirection.WEST;

public class LocationEditor extends AbstractEditor<Location> {
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
    private JComboBox upCombo;
    private JComboBox downCombo;
    private JComboBox northEastCombo;
    private JComboBox southEastCombo;
    private JComboBox southWestCombo;
    private JComboBox northWestCombo;
    private Map<ExitDirection, JComboBox> exitCombosMap = new HashMap<ExitDirection, JComboBox>() {
        {
            put(NORTH, northCombo);
            put(NORTH_EAST, northEastCombo);
            put(EAST, eastCombo);
            put(SOUTH_EAST, southEastCombo);
            put(SOUTH, southCombo);
            put(SOUTH_WEST, southWestCombo);
            put(WEST, westCombo);
            put(NORTH_WEST, northWestCombo);
            put(UP, upCombo);
            put(DOWN, downCombo);
        }
    };
    private boolean toGenerateId = false;
    private ArrayList<Item> itemsClone = null;
    private EventList<Attribute> attributesClone = null;
    private EventList<Hook> hooksClone = null;
    private Location location;
    private Story.DataHelper storyDataHelper;

    public LocationEditor(Window owner, Location location, final Story.DataHelper storyDataHelper) {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor(LOCATION_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        this.location = location;

        addItemButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Item item = new Item();
                if (editItem(item)) {
                    itemsClone.add(item);
                    updateItems();
                    itemsList.setSelectedValue(item, true);
                }
            }
        });
        editItemButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                itemsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!itemsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                editItem((Item) itemsList.getSelectedValue());
            }


        });
        delItemButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                itemsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!itemsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Item item = (Item) itemsList.getSelectedValue();
                if (item != null) {
                    int answer = JOptionPane.showConfirmDialog(LocationEditor.this, "Вы уверены, что хотите удалить этот предмет?");
                    if (answer == 0) {
                        itemsClone.remove(item);
                        updateItems();
                    }
                }
            }


        });
        editAttributesButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ObjectAttributesEditor objectAttributesEditor = new ObjectAttributesEditor(LocationEditor.this, attributesClone,
                        storyDataHelper);
                if (objectAttributesEditor.showDialog()) {
                    updateAttributes();
                }
            }
        });

        // hooks
        addHookButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Hook hook = new Hook();
                if (editHook(hook)) {
                    hooksClone.add(hook);
                }
            }
        });
        editHookButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                setEnabled(false); // initially disabled
                hooksList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!hooksList.isSelectionEmpty()); // dependent from selection
                    }
                });
            }

            @Override()
            public void actionPerformed(ActionEvent e) {
                Hook selectedHook = (Hook) hooksList.getSelectedValue();
                if (selectedHook != null) {
                    editHook(selectedHook);
                }
            }


        });
        deleteHookButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                setEnabled(false); // initially disabled
                hooksList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!hooksList.isSelectionEmpty()); // dependent from selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Hook selectedHook = (Hook) hooksList.getSelectedValue();
                if (selectedHook != null && JOptionPane.showConfirmDialog(LocationEditor.this,
                        "Вы действительно хотите удалить выбранный перехват?", "Удаление перехвата", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    hooksClone.remove(selectedHook);
                }
            }


        });

        itemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Item item = (Item) itemsList.getSelectedValue();
                    if (item != null) {
                        editItem(item);
                    }
                }
            }
        });

        locationNameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateId();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateId();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //do nothing
            }
        });

        locationIDText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() != '\0') {
                    toGenerateId = false;
                }
            }
        });

        updateDictionaryLinks(storyDataHelper.getDictionary());
        //TODO:dictWordCombo.setSelectedItem(location.getWord());

        for (ExitDirection exitDirection : ExitDirection.values()) {
            JComboBox comboBox = exitCombosMap.get(exitDirection);
            comboBox.setModel(new GUIUtils.EventComboBoxModelWithNullElement<Location>(storyDataHelper.getLocations(),
                    location.getExit(exitDirection)));
        }

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
        hooksClone = GlazedLists.eventList(location.getHooks());
        hooksList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Hook selectedHook = (Hook) hooksList.getSelectedValue();
                    if (selectedHook != null) {
                        editHook(selectedHook);
                    }
                }
            }
        });
        hooksList.setModel(new DefaultEventListModel<Hook>(hooksClone));
    }

    private boolean editHook(Hook hook) {
        try {
            HookEditor hookEditor = new HookEditor(LocationEditor.this, hook, false, storyDataHelper);
            if (hookEditor.showDialog()) {
                hookEditor.updateData(hook);
                return true;
            }
        } catch (IFML2Exception e) {
            GUIUtils.showErrorMessage(this, e);
        }
        return false;
    }

    private void updateId() {
        if (toGenerateId) {
            locationIDText.setText(storyDataHelper.generateIdByName(locationNameText.getText(), Location.class));
        }
    }

    private void updateAttributes() {
        attributesList.setListData(attributesClone.toArray());
    }

    private void updateItems() {
        DefaultListModel itemsListModel = new DefaultListModel();
        for (Item item : itemsClone) {
            itemsListModel.addElement(item);
        }
        itemsList.setModel(itemsListModel);
    }

    private void updateDictionaryLinks(HashMap<String, Word> dictionary) {
        dictWordCombo.setModel(new DefaultComboBoxModel(dictionary.values().toArray()));
        dictWordCombo.insertItemAt(null, 0);
    }

    private boolean editItem(Item item) {
        if (item != null) {
            ItemEditor itemEditor = new ItemEditor(LocationEditor.this, item, storyDataHelper);
            if (itemEditor.showDialog()) {
                itemEditor.updateData(item);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check name
        if (locationNameText.getText().trim().length() == 0) {
            throw new DataNotValidException("У локации должно быть задано имя.", locationNameText);
        }

        // check id
        String id = locationIDText.getText().trim();
        if (id.length() == 0) {
            throw new DataNotValidException("У локации должен быть задан идентификатор.", locationIDText);
        }

        Object object = storyDataHelper.findObjectById(id);
        if (object != null && !object.equals(location)) {
            String className = null;
            try {
                className = storyDataHelper.getObjectClassName(object);
            } catch (IFML2Exception e) {
                GUIUtils.showErrorMessage(this, e);
            }
            throw new DataNotValidException(
                    "У локации должен быть уникальный идентификатор - не пересекаться с другими локациями, предметами и словарём.\n" +
                            MessageFormat.format("Найден другой объект с тем же идентификатором: \"{0}\" типа \"{1}\".", object, className),
                    locationIDText);
        }
    }

    @Override
    public void updateData(@NotNull Location location) {
        for (ExitDirection exitDirection : ExitDirection.values()) {
            JComboBox combo = exitCombosMap.get(exitDirection);
            location.setExit(exitDirection, (Location) combo.getSelectedItem());
        }

        location.setItems(itemsClone);
        location.setAttributes(attributesClone);

        location.setName(locationNameText.getText().trim());
        location.setId(locationIDText.getText().trim());
        location.setDescription(descriptionText.getText());
        location.setHooks(GlazedLists.eventList(hooksClone)); // rewrite data in EventList
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(500, 480));
        contentPane.setPreferredSize(new Dimension(700, 480));
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
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Основные свойства", panel3);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(4, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(10, 2, new Insets(0, 10, 0, 0), -1, -1));
        scrollPane1.setViewportView(panel4);
        final JLabel label1 = new JLabel();
        label1.setText("Север");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        northCombo = new JComboBox();
        panel4.add(northCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Восток");
        panel4.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eastCombo = new JComboBox();
        panel4.add(eastCombo, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Юг");
        panel4.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        southCombo = new JComboBox();
        panel4.add(southCombo, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Запад");
        panel4.add(label4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        westCombo = new JComboBox();
        panel4.add(westCombo, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Вверх");
        panel4.add(label5, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Вниз");
        panel4.add(label6, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        upCombo = new JComboBox();
        panel4.add(upCombo, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downCombo = new JComboBox();
        panel4.add(downCombo, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Северо-Восток");
        panel4.add(label7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Юго-Восток");
        panel4.add(label8, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Юго-Запад");
        panel4.add(label9, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Северо-Запад");
        panel4.add(label10, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        northEastCombo = new JComboBox();
        panel4.add(northEastCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        southEastCombo = new JComboBox();
        panel4.add(southEastCombo, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        southWestCombo = new JComboBox();
        panel4.add(southWestCombo, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        northWestCombo = new JComboBox();
        panel4.add(northWestCombo, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setVisible(false);
        panel3.add(panel5, new GridConstraints(0, 2, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Предметы:"));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel5.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        itemsList = new JList();
        scrollPane2.setViewportView(itemsList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel5.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addItemButton = new JButton();
        addItemButton.setText("Добавить...");
        addItemButton.setMnemonic('Д');
        addItemButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(addItemButton);
        editItemButton = new JButton();
        editItemButton.setText("Редактировать...");
        editItemButton.setMnemonic('Е');
        editItemButton.setDisplayedMnemonicIndex(1);
        toolBar1.add(editItemButton);
        delItemButton = new JButton();
        delItemButton.setText("Удалить...");
        delItemButton.setMnemonic('У');
        delItemButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(delItemButton);
        final JLabel label11 = new JLabel();
        label11.setText("Словарь:");
        label11.setVisible(false);
        panel3.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dictWordCombo = new JComboBox();
        dictWordCombo.setVisible(false);
        panel3.add(dictWordCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(277, 23), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Идентификатор:");
        panel3.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        locationIDText = new JFormattedTextField();
        panel3.add(locationIDText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(277, 20), null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Название:");
        panel3.add(label13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        locationNameText = new JTextField();
        panel3.add(locationNameText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(277, 20), null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Описание:");
        panel3.add(label14, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(53, 114), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Переходы");
        panel3.add(label15, new GridConstraints(4, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(52, 58), null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel3.add(scrollPane3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(277, 114), null, 0, false));
        descriptionText = new JTextArea();
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        scrollPane3.setViewportView(descriptionText);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setVisible(false);
        panel3.add(panel6, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder("Признаки локации"));
        final JScrollPane scrollPane4 = new JScrollPane();
        panel6.add(scrollPane4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        attributesList = new JList();
        scrollPane4.setViewportView(attributesList);
        editAttributesButton = new JButton();
        editAttributesButton.setText("Изменить...");
        editAttributesButton.setMnemonic('М');
        editAttributesButton.setDisplayedMnemonicIndex(2);
        panel6.add(editAttributesButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Перехваты действий", panel7);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel7.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addHookButton = new JButton();
        addHookButton.setText("Добавить...");
        addHookButton.setMnemonic('Д');
        addHookButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(addHookButton);
        editHookButton = new JButton();
        editHookButton.setText("Изменить...");
        editHookButton.setMnemonic('Е');
        editHookButton.setDisplayedMnemonicIndex(3);
        toolBar2.add(editHookButton);
        deleteHookButton = new JButton();
        deleteHookButton.setText("Удалить");
        deleteHookButton.setMnemonic('У');
        deleteHookButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(deleteHookButton);
        final JScrollPane scrollPane5 = new JScrollPane();
        panel7.add(scrollPane5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        hooksList = new JList();
        scrollPane5.setViewportView(hooksList);
        label1.setLabelFor(northCombo);
        label2.setLabelFor(eastCombo);
        label3.setLabelFor(southCombo);
        label4.setLabelFor(westCombo);
        label5.setLabelFor(upCombo);
        label6.setLabelFor(downCombo);
        label7.setLabelFor(northEastCombo);
        label8.setLabelFor(southEastCombo);
        label9.setLabelFor(southWestCombo);
        label10.setLabelFor(northWestCombo);
        label11.setLabelFor(dictWordCombo);
        label12.setLabelFor(locationIDText);
        label13.setLabelFor(locationNameText);
        label14.setLabelFor(descriptionText);
        label15.setLabelFor(scrollPane1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
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
