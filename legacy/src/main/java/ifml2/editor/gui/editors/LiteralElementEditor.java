package ifml2.editor.gui.editors;

import static ifml2.om.Word.Gender.MASCULINE;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.LiteralTemplateElement;
import ifml2.om.Parameter;
import ifml2.om.Procedure;

public class LiteralElementEditor extends AbstractEditor<LiteralTemplateElement> {
    private static final String EDITOR_TITLE = "Литерал";
    private static final String PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG = "Если выставлена галочка передачи параметра, то параметр должен быть выбран.";
    private final EventList<String> synonymsClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList synonymsList;
    private JButton addButton;
    private JButton editButton;
    private JButton delButton;
    private JComboBox comboParameter;
    private JCheckBox checkUseParameter;

    public LiteralElementEditor(Window owner, LiteralTemplateElement element, Procedure procedure) {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // set actions
        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String synonym = JOptionPane.showInputDialog(LiteralElementEditor.this, "Введите синоним:");
                if (synonym != null && !"".equals(synonym)) {
                    synonymsClone.add(synonym);
                    synonymsList.setSelectedValue(synonym, true);
                }
            }
        });
        editButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                GUIUtils.makeActionDependentFromJList(this, synonymsList);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                editCurrentSynonym();
            }
        });
        delButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                GUIUtils.makeActionDependentFromJList(this, synonymsList);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSynonym = (String) synonymsList.getSelectedValue();
                if (selectedSynonym != null && GUIUtils.showDeleteConfirmDialog(LiteralElementEditor.this, "синоним",
                        "синонима", MASCULINE)) {
                    synonymsClone.remove(selectedSynonym);
                }
            }
        });

        // listeners
        checkUseParameter.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                comboParameter.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        synonymsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editCurrentSynonym();
                }
            }
        });

        // clone data
        synonymsClone = GlazedLists.eventList(element.getSynonyms());

        // load data
        synonymsList.setModel(new DefaultEventListModel<String>(synonymsClone));

        if (procedure != null) {
            comboParameter.setModel(new DefaultEventComboBoxModel<Parameter>(procedure.getParameters()));
        }
        String parameter = element.getParameter();
        if (procedure != null && parameter != null) {
            checkUseParameter.setSelected(true);
            comboParameter.setSelectedItem(procedure.getParameterByName(parameter));
        } else {
            checkUseParameter.setSelected(false);
        }
    }

    private void editCurrentSynonym() {
        String selectedSynonym = (String) synonymsList.getSelectedValue();
        if (selectedSynonym != null) {
            String editedSynonym = JOptionPane.showInputDialog(this, "Исправьте синоним:", selectedSynonym);
            if (editedSynonym != null && !"".equals(editedSynonym)) {
                synonymsClone.set(synonymsList.getSelectedIndex(), editedSynonym);
            }
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check if check box is set then parameter is selected
        if (checkUseParameter.isSelected() && comboParameter.getSelectedItem() == null) {
            throw new DataNotValidException(PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG, comboParameter);
        }
    }

    @Override
    public void updateData(@NotNull LiteralTemplateElement data) throws IFML2EditorException {
        EventList<String> synonyms = data.getSynonyms();
        synonyms.clear();
        synonyms.addAll(synonymsClone);
        Parameter selectedItem = (Parameter) comboParameter.getSelectedItem();
        data.setParameter(checkUseParameter.isSelected() && selectedItem != null ? selectedItem.toString() : null);
    }
}
