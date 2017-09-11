package ifml2.editor.gui.forms.expressions;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberExpressionEditForm extends ExpressionEditForm {
    private final String numberLiteralRegEx = "(\\d+)";
    private Pattern pattern = Pattern.compile(numberLiteralRegEx);
    private JPanel contentPane;
    private JRadioButton numberRadioButton;
    private JRadioButton expressionRadioButton;
    private JTextArea expressionTextArea;
    private JSpinner numberSpinner;

    public NumberExpressionEditForm(String expression) {
        super(expression);
        setContentPane(contentPane);

        numberRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                numberSpinner.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        expressionRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                expressionTextArea.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
    }

    @Override
    protected void bindData() {
        expressionTextArea.setText(expression);

        if (expression == null || pattern.matcher(expression).matches()) {
            numberSpinner.setValue(expression != null ? extractNumber(expression) : 0);
            numberRadioButton.setSelected(true);
        } else {
            expressionRadioButton.setSelected(true);
        }
    }

    private Integer extractNumber(String expression) {
        final Matcher matcher = pattern.matcher(expression);
        return Integer.parseInt(matcher.group(1));
    }

    @Override
    public String getEditedExpression() {
        return numberRadioButton.isSelected() ? String.valueOf(numberSpinner.getValue()) : expressionTextArea.getText();
    }
}
