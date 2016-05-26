package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Action;
import ifml2.om.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static ifml2.GUIUtils.showErrorMessage;

public class ActionEditor extends AbstractEditor<Action> {
    private Action actionClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JTextField descriptionText;
    private JComboBox<Procedure> procedureCallCombo;
    private ListEditForm<Template> templatesListEditForm;
    private ListEditForm<Restriction> restrictionsListEditForm;
    private Story.DataHelper storyDataHelper;

    public ActionEditor(Window owner, @NotNull Action action, Story.DataHelper storyDataHelper) {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor("Действие", contentPane, buttonOK, buttonCancel);

        // clone whole entity
        try {
            actionClone = action.clone();
        } catch (CloneNotSupportedException e) {
            //GUIUtils.showErrorMessage(this, e);
            throw new RuntimeException(e);
        }

        // bind data
        templatesListEditForm.bindData(actionClone.getTemplates());
        restrictionsListEditForm.bindData(actionClone.getRestrictions());

        // init form data
        nameText.setText(action.getName());
        descriptionText.setText(action.getDescription());
        procedureCallCombo.setModel(new DefaultEventComboBoxModel<>(storyDataHelper.getProcedures()));
        procedureCallCombo.setSelectedItem(action.getProcedure());
    }

    private boolean editRestriction(Restriction restriction) {
        RestrictionEditor restrictionEditor = new RestrictionEditor(this, restriction, storyDataHelper);
        if (restrictionEditor.showDialog()) {
            try {
                restrictionEditor.updateData(restriction);
                return true;
            } catch (IFML2EditorException e) {
                showErrorMessage(this, e);
            }
        }
        return false;
    }

    private boolean editTemplate(@NotNull Template template) {
        Procedure selectedProcedure = (Procedure) procedureCallCombo.getSelectedItem();
        TemplateEditor templateEditor = new TemplateEditor(this, template, selectedProcedure);
        if (templateEditor.showDialog()) {
            try {
                templateEditor.updateData(template);
                return true;
            } catch (IFML2EditorException e) {
                showErrorMessage(this, e);
            }
        }
        return false;
    }

    @Override
    public void updateData(@NotNull Action action) throws IFML2EditorException {
        actionClone.setName(nameText.getText());
        actionClone.setDescription(descriptionText.getText());
        actionClone.setProcedure((Procedure) procedureCallCombo.getSelectedItem());

        try {
            actionClone.copyTo(action);
        } catch (CloneNotSupportedException e) {
            showErrorMessage(this, e);
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (nameText.getText().trim().length() == 0) {
            throw new DataNotValidException("У действия должно быть имя!", nameText);
        }
    }

    private void createUIComponents() {
        templatesListEditForm = new ListEditForm<Template>(this, "шаблон", "шаблона", Word.Gender.MASCULINE, Template.class) {
            @Override
            protected Template createElement() throws Exception {
                Template template = new Template();
                return editElement(template) ? template : null;
            }

            @Override
            protected boolean editElement(Template selectedElement) throws Exception {
                return editTemplate(selectedElement);
            }
        };

        restrictionsListEditForm = new ListEditForm<Restriction>(this, "ограничение", "ограничения", Word.Gender.NEUTER, Restriction.class) {
            @Override
            protected Restriction createElement() throws Exception {
                Restriction restriction = new Restriction();
                return editRestriction(restriction) ? restriction : null;
            }

            @Override
            protected boolean editElement(Restriction selectedElement) throws Exception {
                return editRestriction(selectedElement);
            }
        };
    }
}
