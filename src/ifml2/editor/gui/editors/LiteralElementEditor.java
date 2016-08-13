package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.LiteralTemplateElement;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

import static ifml2.om.Word.Gender.MASCULINE;

public class LiteralElementEditor extends AbstractEditor<LiteralTemplateElement> {
    private static final String EDITOR_TITLE = "Литерал";
    private static final String PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG = "Если выставлена галочка передачи параметра, то параметр должен быть выбран.";
    private final LiteralTemplateElement elementClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<Parameter> comboParameter;
    private JCheckBox checkUseParameter;
    private ListEditForm<String> synonymsListEditForm;

    LiteralElementEditor(Window owner, LiteralTemplateElement element, Procedure procedure) throws IFML2EditorException {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // listeners
        checkUseParameter.addItemListener(e -> comboParameter.setEnabled(e.getStateChange() == ItemEvent.SELECTED));

        // clone whole entity
        try {
            elementClone = element.clone();
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Ошибка при клонировании литерального элемента: {0}", e.toString());
        }

        // bind data
        bindData();

        // load data
        if (procedure != null) {
            comboParameter.setModel(new DefaultEventComboBoxModel<>(procedure.getParameters()));
        }
        String parameter = element.getParameter();
        if (procedure != null && parameter != null) {
            checkUseParameter.setSelected(true);
            comboParameter.setSelectedItem(procedure.getParameterByName(parameter));
        } else {
            checkUseParameter.setSelected(false);
        }
    }

    private void bindData() {
        synonymsListEditForm.bindData(elementClone.getSynonyms());
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check if check box is set then parameter is selected
        if (checkUseParameter.isSelected() && comboParameter.getSelectedItem() == null) {
            throw new DataNotValidException(PARAMETER_MUST_BE_SET_ERROR_MESSAGE_DIALOG, comboParameter);
        }
    }

    @Override
    public void updateData(@NotNull LiteralTemplateElement element) throws IFML2EditorException {
        Parameter selectedItem = (Parameter) comboParameter.getSelectedItem();
        elementClone.setParameter(checkUseParameter.isSelected() && selectedItem != null ? selectedItem.toString() : null);
        elementClone.copyTo(element);
    }

    private void createUIComponents() {
        synonymsListEditForm = new ListEditForm<String>(this, "синоним", "синонима", MASCULINE) {
            @Override
            protected String createElement() throws Exception {
                String synonym = JOptionPane.showInputDialog(LiteralElementEditor.this, "Введите синоним:");
                if (synonym != null && !"".equals(synonym)) {
                    // TODO: 13.08.2016 forbid add doubles
                    return synonym;
                }
                return null;
            }

            @Override
            protected boolean editElement(String selectedElement, @NotNull Consumer<String> elementUpdater) throws Exception {
                if (selectedElement != null) {
                    String editedSynonym = JOptionPane.showInputDialog(this, "Исправьте синоним:", selectedElement);
                    if (editedSynonym != null && !"".equals(editedSynonym)) {
                        // TODO: 13.08.2016 forbid change to doubles
                        elementUpdater.accept(editedSynonym);
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
