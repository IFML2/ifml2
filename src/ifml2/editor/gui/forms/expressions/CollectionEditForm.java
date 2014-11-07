package ifml2.editor.gui.forms.expressions;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.IFMLObject;
import ifml2.om.Story;
import ifml2.om.Word;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

public class CollectionEditForm extends JInternalFrame
{
    private final EventList<IFMLObject> editedCollection;
    private JPanel contentPane;
    private ListEditForm<IFMLObject> collectionListEditForm;
    private Window owner;
    private Class<? extends IFMLObject> filterByClass;
    private Story.DataHelper dataHelper;

    public CollectionEditForm(Window owner, EventList<IFMLObject> collectionItems, Class<? extends IFMLObject> filterByClass,
            Story.DataHelper dataHelper)
    {
        this.owner = owner;
        this.filterByClass = filterByClass;
        this.dataHelper = dataHelper;
        setContentPane(contentPane);

        editedCollection = GlazedLists.eventList(collectionItems);

        collectionListEditForm.bindData(editedCollection);
    }

    private void createUIComponents()
    {
        collectionListEditForm = new ListEditForm<IFMLObject>(owner, "элемент", "элемента", Word.GenderEnum.MASCULINE, IFMLObject.class)
        {
            {
                showEditButton = false;
            }

            @Override
            protected IFMLObject createElement() throws Exception
            {
                final Collection<IFMLObject> allObjects = dataHelper.getCopyOfAllObjects();

                // filter objects by type
                for (Iterator<IFMLObject> iterator = allObjects.iterator(); iterator.hasNext(); )
                {
                    IFMLObject object = iterator.next();
                    if (!filterByClass.isInstance(object))
                    {
                        iterator.remove();
                    }
                }

                return (IFMLObject) JOptionPane
                        .showInputDialog(owner, "Выберите объект для добавления", "Новый элемент коллекции", JOptionPane.QUESTION_MESSAGE,
                                null, allObjects.toArray(), null);
            }
        };
    }

    public EventList<IFMLObject> getEditedCollection()
    {
        return editedCollection;
    }
}
