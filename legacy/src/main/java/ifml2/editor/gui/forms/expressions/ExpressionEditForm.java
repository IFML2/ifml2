package ifml2.editor.gui.forms.expressions;

import javax.swing.*;

public abstract class ExpressionEditForm extends JInternalFrame {
    protected String expression;

    protected ExpressionEditForm(String expression) {
        this.expression = expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        bindData();
    }

    protected abstract void bindData();

    public abstract String getEditedExpression();
}
