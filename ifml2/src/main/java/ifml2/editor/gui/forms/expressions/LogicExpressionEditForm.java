package ifml2.editor.gui.forms.expressions;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ifml2.SystemIdentifiers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogicExpressionEditForm extends ExpressionEditForm {
    private JPanel contentPane;
    private JRadioButton logicRadioButton;
    private JRadioButton expressionRadioButton;
    private JRadioButton yesRadioButton;
    private JRadioButton noRadioButton;
    private JTextArea expressionTextArea;
    private String logicLiteralRegEx = "([Дд][Аа]|[Нн][Ее][Тт])";
    private Pattern pattern = Pattern.compile(logicLiteralRegEx);

    public LogicExpressionEditForm(String expression) {
        super(expression);
        setContentPane(contentPane);

        logicRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final boolean toEnable = e.getStateChange() == ItemEvent.SELECTED;
                yesRadioButton.setEnabled(toEnable);
                noRadioButton.setEnabled(toEnable);
            }
        });
        expressionRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                expressionTextArea.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        bindData();
    }

    @Override
    protected void bindData() {
        expressionTextArea.setText(expression);

        if (expression == null || pattern.matcher(expression).matches()) {
            Boolean logic = expression != null ? extractLogic(expression) : false;

            if (logic) {
                yesRadioButton.setSelected(true);
            } else {
                noRadioButton.setSelected(true);
            }

            logicRadioButton.setSelected(true);
        } else {
            expressionRadioButton.setSelected(true);
        }
    }

    private Boolean extractLogic(String expression) {
        Matcher matcher = pattern.matcher(expression);
        return matcher.matches() && SystemIdentifiers.TRUE_BOOL_LITERAL.equalsIgnoreCase(matcher.group(1));
    }

    @Override
    public String getEditedExpression() {
        return logicRadioButton.isSelected() ? createLiteral(yesRadioButton.isSelected()) : expressionTextArea.getText();
    }

    private String createLiteral(boolean logic) {
        return logic ? SystemIdentifiers.TRUE_BOOL_LITERAL : SystemIdentifiers.FALSE_BOOL_LITERAL;
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
