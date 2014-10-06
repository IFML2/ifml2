package ifml2.editor.gui.forms.expressions;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogicExpressionEditForm extends ExpressionEditForm
{
    private final String yesLiteral = "да";
    private JPanel contentPane;
    private JRadioButton logicRadioButton;
    private JRadioButton expressionRadioButton;
    private JRadioButton yesRadioButton;
    private JRadioButton noRadioButton;
    private JTextArea expressionTextArea;
    private String logicLiteralRegEx = "([Дд][Аа]|[Нн][Ее][Тт])";
    private Pattern pattern = Pattern.compile(logicLiteralRegEx);

    public LogicExpressionEditForm(String expression)
    {
        super(expression);
        setContentPane(contentPane);

        //fixme control enable states

        //fixme change expression by logic value

        bindData();
    }

    @Override
    protected void bindData()
    {
        expressionTextArea.setText(expression);

        if (expression != null && pattern.matcher(expression).matches())
        {
            Boolean logic = extractLogic(expression);

            if (logic)
            {
                yesRadioButton.setSelected(true);
            }
            else
            {
                noRadioButton.setSelected(true);
            }

            logicRadioButton.setSelected(true);
        }
        else
        {
            expressionRadioButton.setSelected(true);
        }
    }

    private Boolean extractLogic(String expression)
    {
        Matcher matcher = pattern.matcher(expression);
        return matcher.matches() && yesLiteral.equalsIgnoreCase(matcher.group(1));
    }

    @Override
    public String getEditedExpression()
    {
        return null;
    }
}
