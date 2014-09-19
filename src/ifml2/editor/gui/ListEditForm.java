package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
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

/**
 * JInternalFrame with JList and buttons <b>Add</b>, <b>Edit</b>, <b>Delete</b> and optional <b>Up</b> and <b>Down</b> arrows.<br/>
 * Usage: add to form, tick "Custom create" property, create and override abstract methods <i>createElement()</i>,
 * <i>editElement(T selectedElement)</i> and, if needed, <i>beforeDelete(T selectedElement)</i>. Call bindData(EventList<T> clonedList)
 * then you need update data in list.
 *
 * @param <T> Edited type.
 */
public abstract class ListEditForm<T> extends JInternalFrame
{
    protected Window owner;
    /**
     * Flag to show or hide toolbar with up/down buttons. Set it to other value in static constructor to change behaviour.
     */
    protected boolean showUpDownButtons = true;
    private JPanel contentPane;
    private JList elementsList;
    private JButton upButton;
    private JButton downButton;
    private JButton addElementButton;
    private JButton editElementButton;
    private JButton delElementButton;
    private JToolBar upDownToolbar;
    private String objectNameVP;
    private String objectNameRP;
    private Word.GenderEnum gender;
    private JPopupMenu popupMenu;
    private EventList<T> clonedList;
    private java.util.List<ChangeListener> listChangeListeners = new ArrayList<ChangeListener>();
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
            @NotNull final Word.GenderEnum gender, Class<T> clazz)
    {
        setContentPane(contentPane);

        this.clazz = clazz;
        this.owner = owner;
        this.objectNameVP = objectNameVP;
        this.objectNameRP = objectNameRP;
        this.gender = gender;

        // init buttons
        final ButtonAction addButtonAction = new ButtonAction(addElementButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    T element = createElement();
                    if (element != null)
                    {
                        clonedList.add(element);
                        elementsList.setSelectedValue(element, true);
                        fireListChangeListeners();
                    }
                }
                catch (Exception ex)
                {
                    GUIUtils.showErrorMessage(owner, ex);
                }
            }
        };
        addElementButton.setAction(addButtonAction);

        final ButtonAction editButtonAction = new ButtonAction(editElementButton, elementsList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doEditElement(getSelectedElement());
            }
        };
        editElementButton.setAction(editButtonAction);

        final ButtonAction delButtonAction = new ButtonAction(delElementButton, elementsList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                T selectedElement = getSelectedElement();
                int selectedIndex = elementsList.getSelectedIndex();

                Boolean toRemove = false;

                try
                {
                    toRemove = beforeDelete(selectedElement);
                }
                catch (Exception ex)
                {
                    GUIUtils.showErrorMessage(owner, ex);
                }

                if (toRemove)
                {
                    clonedList.remove(selectedElement);

                    selectedIndex = Math.min(selectedIndex, elementsList.getModel().getSize() - 1); // make index not exceeding limit
                    elementsList.setSelectedIndex(selectedIndex);

                    fireListChangeListeners();
                }
            }
        };
        delElementButton.setAction(delButtonAction);

        // init popup menu
        popupMenu = new JPopupMenu()
        {
            {
                add(addButtonAction);
                addSeparator();
                add(editButtonAction);
                add(delButtonAction);
            }
        };

        // double and right clicks
        elementsList.addMouseListener(new MouseAdapter()
        {
            // right click show popup
            @Override
            public void mouseReleased(MouseEvent e)
            {
                showPopup(e);
            }

            // right click show popup
            @Override
            public void mousePressed(MouseEvent e)
            {
                showPopup(e);
            }

            public void showPopup(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            // double click edits
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    doEditElement(getSelectedElement());
                }
            }
        });

        upButton.setAction(new ButtonAction(upButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selIdx = elementsList.getSelectedIndex();
                if (selIdx > 0)
                {
                    Collections.swap(clonedList, selIdx, selIdx - 1);
                    elementsList.setSelectedIndex(selIdx - 1);
                    fireListChangeListeners();
                }
            }

            @Override
            public void init()
            {
                updateState();

                elementsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        updateState();
                    }
                });
            }

            private void updateState()
            {
                setEnabled(elementsList.getSelectedIndex() > 0);
            }
        });

        downButton.setAction(new ButtonAction(downButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selIdx = elementsList.getSelectedIndex();
                if (selIdx < clonedList.size() - 1)
                {
                    Collections.swap(clonedList, selIdx, selIdx + 1);
                    elementsList.setSelectedIndex(selIdx + 1);
                    fireListChangeListeners();
                }
            }

            @Override
            public void init()
            {
                updateState();

                elementsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        updateState();
                    }
                });
            }

            private void updateState()
            {
                int selectedInstrIdx = elementsList.getSelectedIndex();
                int listSize = elementsList.getModel().getSize();
                setEnabled(selectedInstrIdx < listSize - 1);
            }
        });

        // init form
        upDownToolbar.setVisible(showUpDownButtons);
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
     *
     * @param selectedElement currently selected element.
     * @return true if edit was made and false vise versa.
     * @throws Exception will be shown.
     */
    protected abstract boolean editElement(T selectedElement) throws Exception;

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
    protected boolean beforeDelete(T selectedElement) throws Exception
    {
        return showDeleteConfirmDialog();
    }

    /**
     * Adds list change listened. Event of change fired then elements are added, edited, deleted or swapped (using arrow buttons).
     *
     * @param changeListener ChangeListener to receive events.
     */
    public void addListChangeListener(ChangeListener changeListener)
    {
        listChangeListeners.add(changeListener);
    }

    private void fireListChangeListeners()
    {
        for (ChangeListener changeListener : listChangeListeners)
        {
            changeListener.stateChanged(new ChangeEvent(clonedList));
        }
    }

    public boolean showDeleteConfirmDialog()
    {
        return GUIUtils.showDeleteConfirmDialog(owner, objectNameVP, objectNameRP, gender);
    }

    private void doEditElement(@Nullable T selectedElement)
    {
        try
        {
            if (selectedElement != null)
            {
                Boolean isEdited = editElement(selectedElement);
                if (isEdited)
                {
                    elementsList.setSelectedValue(selectedElement, true);
                    fireListChangeListeners();
                }
            }
        }
        catch (Exception ex)
        {
            GUIUtils.showErrorMessage(owner, ex);
        }
    }

    /**
     * Returns selected list element.
     *
     * @return selected list element.
     */
    protected T getSelectedElement()
    {

        final Object selectedValue = elementsList.getSelectedValue();
        return clazz.isInstance(selectedValue) ? clazz.cast(selectedValue) : null;
    }

    /**
     * Binds data and updates form.
     *
     * @param clonedList Already cloned list. All changes are made with him.
     */
    public void bindData(EventList<T> clonedList)
    {
        this.clonedList = clonedList;
        elementsList.setModel(new DefaultEventListModel<T>(clonedList));
    }
}
