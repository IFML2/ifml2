package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private Dialog owner;

    public ListEditForm()
    {
        setContentPane(contentPane);
    }

    /**
     * Initializes form.
     *
     * @param owner             Dialog - owner of form - for showing sub dialog on him.
     * @param clonedList        Already cloned list. All changes are made with him.
     * @param objectNameVP      Element VP.
     * @param objectNameRP      Element RP.
     * @param gender            Element gender.
     * @param showUpDownButtons To show Up-Down buttons toolbar or not.
     * @param addAction         Callable&lt;T&gt; - add action method. Runs when Add button pressed. If it returns not null, add it to list and selects.
     * @param editAction        Callable&lt;Boolean&gt; - edit action method. Runs when Edit button pressed. If it returns true selects it.
     */
    public void init(@Nullable final Dialog owner, @NotNull final EventList<T> clonedList,
        @NotNull final String objectNameVP, @NotNull final String objectNameRP, @NotNull final Word.GenderEnum gender,
        @NotNull final Boolean showUpDownButtons, @Nullable final Callable<T> addAction,
        @Nullable final Callable<Boolean> editAction)
    {
        this.owner = owner;
        this.editAction = editAction;

        // init buttons
        addElementButton.setAction(new ButtonAction(addElementButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (addAction != null)
                {
                    try
                    {
                        T element = addAction.call();
                        if (element != null)
                        {
                            clonedList.add(element);
                            elementsList.setSelectedValue(element, true);
                        }
                    }
                    catch (Exception ex)
                    {
                        GUIUtils.showErrorMessage(owner, ex);
                    }
                }
            }
        });

        editElementButton.setAction(new ButtonAction(editElementButton, elementsList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editElement();
            }
        });

        delElementButton.setAction(new ButtonAction(delElementButton, elementsList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (GUIUtils.showDeleteConfirmDialog(owner, objectNameVP, objectNameRP, gender))
                {
                    T selectedElement = (T) elementsList.getSelectedValue();
                    if (selectedElement != null)
                    {
                        clonedList.remove(selectedElement);
                    }
                }
            }
        });

        elementsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    editElement();
                }
            }
        });

        upDownToolbar.setVisible(showUpDownButtons);

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
                }
            }

            @Override
            public void init()
            {
                if (showUpDownButtons)
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
                }
            }

            @Override
            public void init()
            {
                if (showUpDownButtons)
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
            }

            private void updateState()
            {
                int selectedInstrIdx = elementsList.getSelectedIndex();
                int listSize = elementsList.getModel().getSize();
                setEnabled(selectedInstrIdx < listSize - 1);
            }
        });

        // init form
        elementsList.setModel(new DefaultEventListModel<T>(clonedList));
    }

    private void editElement()
    {
        if (editAction != null)
        {
            try
            {
                Object selectedElement = elementsList.getSelectedValue();
                if (selectedElement != null)
                {
                    Boolean isEdited = editAction.call();
                    if (isEdited)
                    {
                        elementsList.setSelectedValue(selectedElement, true);
                    }
                }
            }
            catch (Exception ex)
            {
                GUIUtils.showErrorMessage(owner, ex);
            }
        }
    }

    public T getSelectedElement()
    {
        return (T) elementsList.getSelectedValue();
    }
}
