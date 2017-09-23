package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setPreferredSize(new Dimension(640, 480));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonCancel.setMnemonic('C');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 4, new Insets(4, 4, 4, 4), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Обработка ошибок парсинга"));
        parseErrorHandlerCombo = new JComboBox();
        panel4.add(parseErrorHandlerCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parseErrorHandlerCreateButton = new JButton();
        parseErrorHandlerCreateButton.setIcon(new ImageIcon(getClass().getResource("/Add24.gif")));
        parseErrorHandlerCreateButton.setText("Создать...");
        parseErrorHandlerCreateButton.setMnemonic('С');
        parseErrorHandlerCreateButton.setDisplayedMnemonicIndex(0);
        panel4.add(parseErrorHandlerCreateButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parseErrorHandlerOpenButton = new JButton();
        parseErrorHandlerOpenButton.setIcon(new ImageIcon(getClass().getResource("/Edit24.gif")));
        parseErrorHandlerOpenButton.setText("Редактировать...");
        parseErrorHandlerOpenButton.setMnemonic('Р');
        parseErrorHandlerOpenButton.setDisplayedMnemonicIndex(0);
        panel4.add(parseErrorHandlerOpenButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Процедура:");
        panel4.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        final JTextArea textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setLineWrap(true);
        textArea1.setText("Можно выбрать или создать процедуру, принимающую два обязательных текстовых параметра: Фраза и Ошибка. Фраза содержит фразу, введённую Игроком, Ошибка - текст ошибки парсинга.\nЕсли процедура вернёт (инструкция \"Вернуть значение\") логическое \"нет\", то продолжится стандартная обработка ошибки (т.е. вывод ошибки Игроку).");
        textArea1.setWrapStyleWord(true);
        scrollPane1.setViewportView(textArea1);
        label1.setLabelFor(parseErrorHandlerCombo);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
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
