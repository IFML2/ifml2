package ifml2.editor.gui.forms;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ifml2.GUIUtils;
import ifml2.editor.gui.ButtonAction;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JInternalFrame with JList and buttons <b>Add</b>, <b>Edit</b>, <b>Delete</b> and optional <b>Up</b> and <b>Down</b> arrows.<br/>
 * Usage: add to form, tick "Custom create" property, create and override abstract methods <i>createElement()</i>,
 * <i>editElement(T selectedElement)</i> and, if needed, <i>beforeDelete(T selectedElement)</i>. Call bindData(EventList<T> clonedList)
 * when you need update data in list.
 *
 * @param <T> Edited type.
 */
public abstract class ListEditForm<T> extends JInternalFrame {
    protected Window owner;
    private EventList<T> clonedList;
    private JPanel contentPane;
    private JList elementsList;
    private JButton upButton;
    private JButton downButton;
    private JButton addElementButton;
    private JButton editElementButton;
    private JButton delElementButton;
    private String objectNameVP;
    private String objectNameRP;
    private Word.Gender gender;
    private JPopupMenu popupMenu;
    private List<ChangeListener> listChangeListeners = new ArrayList<ChangeListener>();
    private Class<T> clazz;

    /**
     * Initializes form.
     *
     * @param owner        Dialog - owner of form - for showing sub dialog on him.
     * @param objectNameVP Element VP.
     * @param objectNameRP Element RP.
     * @param gender       Element gender.
     * @param clazz        T class.
     */
    public ListEditForm(@Nullable final Window owner, @NotNull final String objectNameVP, @NotNull final String objectNameRP,
                        @NotNull final Word.Gender gender, Class<T> clazz) {
        setContentPane(contentPane);

        this.clazz = clazz;
        this.owner = owner;
        this.objectNameVP = objectNameVP;
        this.objectNameRP = objectNameRP;
        this.gender = gender;

        // init buttons
        final ButtonAction addButtonAction = new ButtonAction(addElementButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    T element = createElement();
                    if (element != null) {
                        addElementToList(element);
                        elementsList.setSelectedValue(element, true);
                        fireListChangeListeners();
                    }
                } catch (Exception ex) {
                    GUIUtils.showErrorMessage(owner, ex);
                }
            }
        };
        addElementButton.setAction(addButtonAction);

        final ButtonAction editButtonAction = new ButtonAction(editElementButton, elementsList) {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEditElement(getSelectedElement());
            }
        };
        editElementButton.setAction(editButtonAction);

        final ButtonAction delButtonAction = new ButtonAction(delElementButton, elementsList) {
            @Override
            public void actionPerformed(ActionEvent e) {
                T selectedElement = getSelectedElement();
                int selectedIndex = elementsList.getSelectedIndex();

                Boolean toRemove = false;

                try {
                    toRemove = beforeDelete(selectedElement);
                } catch (Exception ex) {
                    GUIUtils.showErrorMessage(owner, ex);
                }

                if (toRemove) {
                    clonedList.remove(selectedElement);

                    selectedIndex = Math.min(selectedIndex, elementsList.getModel().getSize() - 1); // make index not exceeding limit
                    elementsList.setSelectedIndex(selectedIndex);

                    fireListChangeListeners();
                }
            }
        };
        delElementButton.setAction(delButtonAction);

        // init popup menu
        popupMenu = new JPopupMenu() {
            {
                add(addButtonAction);
                addSeparator();
                if (isShowEditButton()) {
                    add(editButtonAction);
                }
                add(delButtonAction);
            }
        };

        // double and right clicks
        elementsList.addMouseListener(new MouseAdapter() {
            // right click show popup
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            // right click show popup
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            public void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            // double click edits
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doEditElement(getSelectedElement());
                }
            }
        });

        upButton.setAction(new ButtonAction(upButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = elementsList.getSelectedIndex();
                if (selIdx > 0) {
                    Collections.swap(clonedList, selIdx, selIdx - 1);
                    elementsList.setSelectedIndex(selIdx - 1);
                    fireListChangeListeners();
                }
            }

            @Override
            public void init() {
                updateState();

                elementsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        updateState();
                    }
                });
            }

            private void updateState() {
                setEnabled(elementsList.getSelectedIndex() > 0);
            }
        });

        downButton.setAction(new ButtonAction(downButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = elementsList.getSelectedIndex();
                if (selIdx < clonedList.size() - 1) {
                    Collections.swap(clonedList, selIdx, selIdx + 1);
                    elementsList.setSelectedIndex(selIdx + 1);
                    fireListChangeListeners();
                }
            }

            @Override
            public void init() {
                updateState();

                elementsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        updateState();
                    }
                });
            }

            private void updateState() {
                int selectedInstrIdx = elementsList.getSelectedIndex();
                int listSize = elementsList.getModel().getSize();
                setEnabled(selectedInstrIdx < listSize - 1);
            }
        });
    }

    /**
     * Add element logic. By default adds element to clonedList. <br/>
     * Override if needed other logic.
     *
     * @param element adding element.
     */
    protected void addElementToList(T element) {
        clonedList.add(element);
    }

    /**
     * Creates element. You should override this method to implement element creation logic.
     * If you return not null, it will be added to list and events fired.
     *
     * @return Return element if created and null if not.
     * @throws Exception will be shown.
     */
    protected abstract T createElement() throws Exception;

    /**
     * Edits element. You should override this method to implement element edition logic.
     * If you return true, list will be updated and events fired.
     * Don't override if you have set showEditButton = false
     *
     * @param selectedElement currently selected element.
     * @return true if edit was made and false vise versa.
     * @throws Exception will be shown.
     */
    protected boolean editElement(T selectedElement) throws Exception {
        return false;
    }

    /**
     * Fired before deletion of element. Returns true if element should be deleted.
     * Override if you need custom logic (verifications before deletion etc).
     * Original implementation just asks about deletion in dialog box.
     * (You can call it to ask with standard dialog.)
     *
     * @param selectedElement currently selected element.
     * @return true if element should be deleted and false vise versa.
     * @throws Exception will be shown.
     */
    protected boolean beforeDelete(T selectedElement) throws Exception {
        return showDeleteConfirmDialog();
    }

    /**
     * Adds list change listened. Event of change fired then elements are added, edited, deleted or swapped (using arrow buttons).
     *
     * @param changeListener ChangeListener to receive events.
     */
    public void addListChangeListener(ChangeListener changeListener) {
        listChangeListeners.add(changeListener);
    }

    private void fireListChangeListeners() {
        for (ChangeListener changeListener : listChangeListeners) {
            changeListener.stateChanged(new ChangeEvent(clonedList));
        }
    }

    public boolean showDeleteConfirmDialog() {
        return GUIUtils.showDeleteConfirmDialog(owner, objectNameVP, objectNameRP, gender);
    }

    private void doEditElement(@Nullable T selectedElement) {
        try {
            if (selectedElement != null) {
                Boolean isEdited = editElement(selectedElement);
                if (isEdited) {
                    elementsList.setSelectedValue(selectedElement, true);
                    fireListChangeListeners();
                }
            }
        } catch (Exception ex) {
            GUIUtils.showErrorMessage(owner, ex);
        }
    }

    /**
     * Returns selected list element.
     *
     * @return selected list element.
     */
    protected T getSelectedElement() {

        final Object selectedValue = elementsList.getSelectedValue();
        return clazz.isInstance(selectedValue) ? clazz.cast(selectedValue) : null;
    }

    /**
     * Binds data and updates form.
     *
     * @param clonedList Already cloned list. All changes are made with him.
     */
    public void bindData(EventList<T> clonedList) {
        this.clonedList = clonedList;
        elementsList.setModel(new DefaultEventListModel<T>(clonedList));
    }

/*    public boolean isShowUpDownButtons()
    {
        return upDownToolbar.isVisible();
    }

    */

    /**
     * Flag to show or hide toolbar with up/down buttons. Set it to other value in static constructor to change behaviour.
     *//*
    public void setShowUpDownButtons(boolean showUpDownButtons)
    {
        upDownToolbar.setVisible(showUpDownButtons);
    }*/
    public boolean isShowEditButton() {
        return editElementButton.isVisible();
    }

    /**
     * Flag to show or hide edit button and edit item in popup menu.
     */
    public void setShowEditButton(boolean showEditButton) {
        editElementButton.setVisible(showEditButton);
    }

    public EventList<T> getClonedList() {
        return clonedList;
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        elementsList = new JList();
        elementsList.setSelectionMode(0);
        scrollPane1.setViewportView(elementsList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOrientation(1);
        panel1.add(toolBar1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        upButton = new JButton();
        upButton.setIcon(new ImageIcon(getClass().getResource("/Up24.gif")));
        upButton.setText("");
        toolBar1.add(upButton);
        downButton = new JButton();
        downButton.setIcon(new ImageIcon(getClass().getResource("/Down24.gif")));
        downButton.setText("");
        toolBar1.add(downButton);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        contentPane.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addElementButton = new JButton();
        addElementButton.setIcon(new ImageIcon(getClass().getResource("/Add24.gif")));
        addElementButton.setText("Добавить...");
        addElementButton.setMnemonic('Д');
        addElementButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(addElementButton);
        editElementButton = new JButton();
        editElementButton.setIcon(new ImageIcon(getClass().getResource("/Edit24.gif")));
        editElementButton.setText("Изменить...");
        editElementButton.setMnemonic('И');
        editElementButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(editElementButton);
        delElementButton = new JButton();
        delElementButton.setIcon(new ImageIcon(getClass().getResource("/Delete24.gif")));
        delElementButton.setText("Удалить...");
        delElementButton.setMnemonic('У');
        delElementButton.setDisplayedMnemonicIndex(0);
        toolBar2.add(delElementButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
