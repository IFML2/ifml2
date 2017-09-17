package ifml2.editor.gui.forms.expressions;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
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
