package ifml2.editor.gui.forms;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.gui.ButtonAction;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * JInternalFrame with JList and buttons <b>Add</b>, <b>Edit</b> (optional), <b>Delete</b> and optional <b>Up</b> and <b>Down</b> arrows.<br/>
 * <i>Usage:</i> add to form, tick "Custom create" property, create and override abstract methods <i>createElement()</i>,
 * <i>editElement(T selectedElement)</i> and, if needed, <i>beforeDelete(T selectedElement)</i>. Call @see<i> #bindData(EventList<T> clonedList)</i>
 * when you need update data in list.
 *
 * @param <T> Edited type.
 * @see #createElement()
 * @see #editElement(Object, Consumer)
 * @see #beforeDelete(T)
 * @see #bindData(EventList)
 * @see #setShowEditButton(boolean)
 * @see #addListChangeListener(ChangeListener)
 */
public abstract class ListEditForm<T> extends JInternalFrame {
    protected Window owner;
    private EventList<T> clonedList;
    private JPanel contentPane;
    private JList<T> elementsList;
    private JButton upButton;
    private JButton downButton;
    private JButton addElementButton;
    private JButton editElementButton;
    private JButton delElementButton;
    private String objectNameVP;
    private String objectNameRP;
    private Word.Gender gender;
    private JPopupMenu popupMenu;
    private List<ChangeListener> listChangeListeners = new ArrayList<>();

    /**
     * Initializes form.
     *
     * @param owner        Dialog - owner of form - for showing sub dialog on him.
     * @param objectNameVP Element VP.
     * @param objectNameRP Element RP.
     * @param gender       Element gender.
     */
    public ListEditForm(@Nullable final Window owner, @NotNull final String objectNameVP, @NotNull final String objectNameRP,
                        @NotNull final Word.Gender gender) {
        setContentPane(contentPane);

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

            void showPopup(MouseEvent e) {
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

                elementsList.addListSelectionListener(e -> updateState());
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
                elementsList.addListSelectionListener(e -> updateState());
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
     * @return Return element if created and null vise versa.
     * @throws Exception will be shown.
     */
    protected abstract T createElement() throws Exception;

    /**
     * Edits element. You should override this method to implement element edition logic.
     * If you return <code>true</code>, list will be updated and events fired.
     * If element type is primitive (not object) you should call provided lambda <code>elementUpdater</code> with edited
     * element to replace it in list.
     * Don't override if you have called <code>setShowEditButton(false)</code>.
     *
     * @param selectedElement currently selected element.
     * @param elementUpdater  replaces element in list, should be called for primitive typed updates.
     * @return <code>true</code> if edit was made and <code>false</code> vise versa.
     * @throws Exception will be shown.
     * @see #setShowEditButton(boolean)
     */
    protected boolean editElement(T selectedElement, Consumer<T> elementUpdater) throws Exception {
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
     * @see #showDeleteConfirmDialog
     */
    protected boolean beforeDelete(T selectedElement) throws Exception {
        return showDeleteConfirmDialog();
    }

    /**
     * Adds list change listened. Event of change fired then elements are added, edited, deleted or swapped (using arrow buttons).
     *
     * @param changeListener ChangeListener to receive events.
     */
    protected void addListChangeListener(ChangeListener changeListener) {
        listChangeListeners.add(changeListener);
    }

    private void fireListChangeListeners() {
        for (ChangeListener changeListener : listChangeListeners) {
            changeListener.stateChanged(new ChangeEvent(clonedList));
        }
    }

    private boolean showDeleteConfirmDialog() {
        return GUIUtils.showDeleteConfirmDialog(owner, objectNameVP, objectNameRP, gender);
    }

    private void doEditElement(@Nullable T selectedElement) {
        try {
            if (selectedElement != null) {
                final int index = clonedList.indexOf(selectedElement);
                Boolean isEdited = editElement(selectedElement, newElement -> clonedList.set(index, newElement));
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
    @Nullable
    private T getSelectedElement() {
        return elementsList.getSelectedValue();
    }

    /**
     * Binds data and updates form.
     *
     * @param clonedList Already cloned list. All changes are made with him.
     */
    public void bindData(@NotNull EventList<T> clonedList) {
        this.clonedList = clonedList;
        elementsList.setModel(new DefaultEventListModel<>(clonedList));
    }

//    public boolean isShowUpDownButtons()
//    {
//        return upDownToolbar.isVisible();
//    }

//    /**
//     * Flag to show or hide toolbar with up/down buttons. Set it to other value in static constructor to change behaviour.
//     */
//    public void setShowUpDownButtons(boolean showUpDownButtons)
//    {
//        upDownToolbar.setVisible(showUpDownButtons);
//    }

    private boolean isShowEditButton() {
        return editElementButton.isVisible();
    }

    /**
     * Flag to show or hide edit button and edit item in popup menu.
     */
    protected void setShowEditButton(boolean showEditButton) {
        editElementButton.setVisible(showEditButton);
    }

    public EventList<T> getClonedList() {
        return clonedList;
    }
}
