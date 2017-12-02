package ifml2.editor.gui.forms.expressions;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import ifml2.vm.values.BooleanValue;

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
        return matcher.matches() && BooleanValue.TRUE.equalsIgnoreCase(matcher.group(1));
    }

    @Override
    public String getEditedExpression() {
        return logicRadioButton.isSelected() ? createLiteral(yesRadioButton.isSelected())
                : expressionTextArea.getText();
    }

    private String createLiteral(boolean logic) {
        return logic ? BooleanValue.TRUE : BooleanValue.FALSE;
    }
}
