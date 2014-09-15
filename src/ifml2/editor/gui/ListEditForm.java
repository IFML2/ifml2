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
import java.util.concurrent.Callable;

/**
 * JInternalFrame with JList and buttons Add, Edit, Delete and optional Up and Down arrows.
 * Usage: add to form and call init(...).
 *
 * @param <T> Edited type.
 */
public class ListEditForm<T> extends JInternalFrame
{
    private JPanel contentPane;
    private JList elementsList;
    private JButton upButton;
    private JButton downButton;
    private JButton addElementButton;
    private JButton editElementButton;
    private JButton delElementButton;
    private JToolBar upDownToolbar;
    private Callable<Boolean> editAction;
    private Window owner;
    private String objectNameVP;
    private String objectNameRP;
    private Word.GenderEnum gender;
    private JPopupMenu popupMenu;
    private EventList<T> clonedList;
    private java.util.List<ChangeListener> listChangeListeners = new ArrayList<ChangeListener>();

    /**
     * Initializes form.
     *
     * @param owner             Dialog - owner of form - for showing sub dialog on him.
     * @param objectNameVP      Element VP.
     * @param objectNameRP      Element RP.
     * @param gender            Element gender.
     * @param showUpDownButtons To show Up-Down buttons toolbar or not.
     * @param addAction         Callable&lt;T&gt; - add action method. Runs when Add button pressed. If it returns not null, add it to list and selects.
     * @param editAction        Callable&lt;Boolean&gt; - edit action method. Runs when Edit button pressed. If it returns true selects it.
     */
    public ListEditForm(@Nullable final Window owner, @NotNull final String objectNameVP, @NotNull final String objectNameRP,
            @NotNull final Word.GenderEnum gender, @NotNull final Boolean showUpDownButtons, @NotNull final Callable<T> addAction,
            @NotNull final Callable<Boolean> editAction, @Nullable final Callable<Boolean> delAction)
    {
        this();

        this.owner = owner;
        this.objectNameVP = objectNameVP;
        this.objectNameRP = objectNameRP;
        this.gender = gender;
        this.editAction = editAction;

        // init buttons
        final ButtonAction addButtonAction = new ButtonAction(addElementButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    T element = addAction.call();
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
                editElement();
            }
        };
        editElementButton.setAction(editButtonAction);

        final ButtonAction delButtonAction = new ButtonAction(delElementButton, elementsList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedIndex = elementsList.getSelectedIndex();
                T selectedElement = getSelectedElement();

                Boolean isDeleted = false;

                if (delAction != null)
                {
                    try
                    {
                        isDeleted = delAction.call();
                    }
                    catch (Exception ex)
                    {
                        GUIUtils.showErrorMessage(owner, ex);
                    }
                }
                else
                {
                    if (showDeleteConfirmDialog())
                    {
                        clonedList.remove(selectedElement);
                        isDeleted = true;
                    }
                }

                if (isDeleted)
                {
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
                    editElement();
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

    private ListEditForm()
    {
        setContentPane(contentPane);
    }

    public ListEditForm(@Nullable final Window owner, @NotNull final String objectNameVP, @NotNull final String objectNameRP,
            @NotNull final Word.GenderEnum gender, @NotNull final Boolean showUpDownButtons, @NotNull final Callable<T> addAction,
            @NotNull final Callable<Boolean> editAction)
    {
        this(owner, objectNameVP, objectNameRP, gender, showUpDownButtons, addAction, editAction, null);
    }

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

    private void editElement()
    {
        if (editAction != null)
        {
            try
            {
                T selectedElement = getSelectedElement();
                if (selectedElement != null)
                {
                    Boolean isEdited = editAction.call();
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
    }

    /**
     * Returns selected list element.
     *
     * @return selected list element.
     */
    public T getSelectedElement()
    {
        return (T) elementsList.getSelectedValue();
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
