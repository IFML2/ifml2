package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.LiteralTemplateElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LiteralElementEditor extends AbstractEditor<LiteralTemplateElement>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList synonymsList;
    private JButton addButton;
    private JButton editButton;
    private JButton delButton;

    private final EventList<String> synonymsClone;

    private static final String EDITOR_TITLE = "Литерал";

    public LiteralElementEditor(Window owner, LiteralTemplateElement element)
    {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // set actions
        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String synonym = JOptionPane.showInputDialog(LiteralElementEditor.this, "Введите синоним:");
                if(synonym != null && !"".equals(synonym))
                {
                    synonymsClone.add(synonym);
                    synonymsList.setSelectedValue(synonym, true);
                }
            }
        });
        editButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                GUIUtils.makeActionDependentFromJList(this, synonymsList);
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String selectedSynonym = (String) synonymsList.getSelectedValue();
                if (selectedSynonym != null)
                {
                    String editedSynonym = JOptionPane.showInputDialog(LiteralElementEditor.this, "Исправьте синоним:", selectedSynonym);
                    if (editedSynonym != null && !"".equals(editedSynonym))
                    {
                        synonymsClone.set(synonymsList.getSelectedIndex(), editedSynonym);
                    }
                }
            }
        });
        delButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                GUIUtils.makeActionDependentFromJList(this, synonymsList);
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String selectedSynonym = (String) synonymsList.getSelectedValue();
                if(selectedSynonym != null && GUIUtils.showDeleteConfirmDialog(LiteralElementEditor.this, "синоним", "синонима"))
                {
                    synonymsClone.remove(selectedSynonym);
                }
            }
        });

        // clone data
        synonymsClone = GlazedLists.eventList(element.getSynonyms());

        // load data
        synonymsList.setModel(new DefaultEventListModel<String>(synonymsClone));
    }

    @Override
    public void getData(@NotNull LiteralTemplateElement data) throws IFML2EditorException
    {
        EventList<String> synonyms = data.getSynonyms();
        synonyms.clear();
        synonyms.addAll(synonymsClone);
    }
}
