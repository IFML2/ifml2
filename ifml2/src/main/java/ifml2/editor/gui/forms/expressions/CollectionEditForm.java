package ifml2.editor.gui.forms.expressions;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Story;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

public class CollectionEditForm extends JInternalFrame {
    private final EventList<IFMLObject> editedCollection;
    private JPanel contentPane;
    private ListEditForm<IFMLObject> collectionListEditForm;
    private Window owner;
    private Class<? extends IFMLObject> filterByClass;
    private Story.DataHelper dataHelper;
    private Item holder;

    public CollectionEditForm(Window owner, @NotNull EventList<IFMLObject> collectionItems, Class<? extends IFMLObject> filterByClass,
                              @NotNull Item holder, Story.DataHelper dataHelper) {
        this.owner = owner;
        this.filterByClass = filterByClass;
        this.dataHelper = dataHelper;
        this.holder = holder;
        setContentPane(contentPane);

        editedCollection = GlazedLists.eventList(collectionItems);

        collectionListEditForm.bindData(editedCollection);
    }

    private void createUIComponents() {
        collectionListEditForm = new ListEditForm<IFMLObject>(owner, "элемент", "элемента", Word.Gender.MASCULINE, IFMLObject.class) {
            {
                setShowEditButton(false);
            }

            @Override
            protected IFMLObject createElement() throws Exception {
                final Collection<IFMLObject> allObjects = dataHelper.getCopyOfAllObjects();

                // filter objects by type and presence in collection
                for (Iterator<IFMLObject> iterator = allObjects.iterator(); iterator.hasNext(); ) {
                    IFMLObject object = iterator.next();
                    if (!filterByClass.isInstance(object) || getClonedList().contains(object) || holder.equals(object)) {
                        iterator.remove();
                    }
                }

                if (!allObjects.isEmpty()) {
                    return (IFMLObject) JOptionPane.showInputDialog(owner, "Выберите объект для добавления", "Новый элемент коллекции",
                            JOptionPane.QUESTION_MESSAGE, null, allObjects.toArray(), null);
                } else {
                    JOptionPane
                            .showMessageDialog(owner, "Не осталось предметов для добавления", "Нет предметов", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
            }


        };
    }

    public EventList<IFMLObject> getEditedCollection() {
        return editedCollection;
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

}
