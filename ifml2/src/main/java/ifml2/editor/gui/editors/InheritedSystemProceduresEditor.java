package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;
import ifml2.CommonConstants;
import ifml2.GUIUtils;
import ifml2.GUIUtils.EventComboBoxModelWithNullElement;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.om.InheritedSystemProcedures;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

public class InheritedSystemProceduresEditor extends AbstractEditor<InheritedSystemProcedures> {
    private static final String INHERITED_SYSTEM_PROCEDURES_EDITOR_FORM_NAME = "Перехваты системных процедур";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox parseErrorHandlerCombo;
    private JButton parseErrorHandlerCreateButton;
    private JButton parseErrorHandlerOpenButton;

    public InheritedSystemProceduresEditor(Window owner, InheritedSystemProcedures inheritedSystemProcedures,
                                           final Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(INHERITED_SYSTEM_PROCEDURES_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // -- init form --

        // create filtered list to avoid unfit procedures
        FilterList<Procedure> filteredProcedures = new FilterList<Procedure>(storyDataHelper.getProcedures(), new Matcher<Procedure>() {
            @Override
            public boolean matches(Procedure item) {
                Parameter phraseParam = item.getParameterByName(CommonConstants.PARSE_ERROR_HANDLER_PRM_PHRASE);
                Parameter errorParam = item.getParameterByName(CommonConstants.PARSE_ERROR_HANDLER_PRM_ERROR);
                return phraseParam != null && errorParam != null;
            }
        });

        parseErrorHandlerCombo.setModel(
                new EventComboBoxModelWithNullElement<Procedure>(filteredProcedures, inheritedSystemProcedures.getParseErrorHandler()));

        //  -- buttons --

        parseErrorHandlerOpenButton.setAction(new ButtonAction(parseErrorHandlerOpenButton, isParseErrorHandlerFilled()) {
            @Override
            public void init() {
                // set start status depends on selection
                parseErrorHandlerCombo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setEnabled(isParseErrorHandlerFilled());
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Procedure procedure = (Procedure) parseErrorHandlerCombo.getSelectedItem();
                if (procedure != null) {
                    ProcedureEditor procedureEditor = new ParseErrorHandlerProcedureEditor(procedure, storyDataHelper);
                    if (procedureEditor.showDialog()) {
                        try {
                            procedureEditor.updateData(procedure);
                        } catch (IFML2EditorException ex) {
                            GUIUtils.showErrorMessage(InheritedSystemProceduresEditor.this, ex);
                        }
                    }
                }
            }
        });
        parseErrorHandlerCreateButton.setAction(new ButtonAction(parseErrorHandlerCreateButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Procedure procedure = new Procedure();
                final EventList<Parameter> parameters = procedure.getParameters();
                parameters.add(new Parameter(CommonConstants.PARSE_ERROR_HANDLER_PRM_PHRASE));
                parameters.add(new Parameter(CommonConstants.PARSE_ERROR_HANDLER_PRM_ERROR));
                ProcedureEditor procedureEditor = new ParseErrorHandlerProcedureEditor(procedure, storyDataHelper);
                if (procedureEditor.showDialog()) {
                    try {
                        procedureEditor.updateData(procedure);
                        storyDataHelper.getProcedures().add(procedure);
                        parseErrorHandlerCombo.setSelectedItem(procedure);
                    } catch (IFML2EditorException ex) {
                        GUIUtils.showErrorMessage(InheritedSystemProceduresEditor.this, ex);
                    }
                }
            }
        });
    }

    private boolean isParseErrorHandlerFilled() {
        return parseErrorHandlerCombo.getSelectedItem() != null;
    }

    @Override
    public void updateData(@NotNull InheritedSystemProcedures data) throws IFML2EditorException {
        data.setParseErrorHandler((Procedure) parseErrorHandlerCombo.getSelectedItem());
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

    /**
     * Override ProcedureEditor to add additional validation
     */
    private class ParseErrorHandlerProcedureEditor extends ProcedureEditor {
        public ParseErrorHandlerProcedureEditor(Procedure procedure, Story.DataHelper storyDataHelper) {
            super(InheritedSystemProceduresEditor.this, procedure, storyDataHelper);
        }

        @Override
        protected void validateData() throws DataNotValidException {
            super.validateData(); // main validation
            // additional validation:
            if (!hasParameter(CommonConstants.PARSE_ERROR_HANDLER_PRM_PHRASE) ||
                    !hasParameter(CommonConstants.PARSE_ERROR_HANDLER_PRM_ERROR)) {
                throw new DataNotValidException(MessageFormat
                        .format("У процедуры должны быть параметры {0} и {1}.", CommonConstants.PARSE_ERROR_HANDLER_PRM_PHRASE,
                                CommonConstants.PARSE_ERROR_HANDLER_PRM_ERROR), paramsEditForm);
            }
        }
    }
}
