package ifml2.editor.gui.forms.expressions;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExpressionEditForm extends ExpressionEditForm
{
    private final String textLiteralRegEx = "'([^']*)'";
    private Pattern pattern = Pattern.compile(textLiteralRegEx);
    private JPanel contentPane;
    private JRadioButton textRadioButton;
    private JRadioButton expressionRadioButton;
    private JTextArea textTextArea;
    private JTextArea expressionTextArea;

    public TextExpressionEditForm(String expression)
    {
        super(expression);
        setContentPane(contentPane);

        textRadioButton.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                textTextArea.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        expressionRadioButton.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                expressionTextArea.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        //fixme change expression while text edits

        bindData();
    }

    @Override
    protected void bindData()
    {
        expressionTextArea.setText(expression);

        if (expression != null && pattern.matcher(expression).matches())
        {
            textTextArea.setText(extractText(expression));
            textRadioButton.setSelected(true);
        }
        else
        {
            expressionRadioButton.setSelected(true);
        }
    }

    @Override
    public String getEditedExpression()
    {
        if (textRadioButton.isSelected())
        {
            return expressionTextArea.getText();
        }
        return crateLiteral(textTextArea.getText());
    }

    private String crateLiteral(String text)
    {
        return "'" + text + "'";
    }

    private String extractText(String expression)
    {
        Matcher matcher = pattern.matcher(expression);
        return matcher.matches() ? matcher.group(1) : "";
    }
}