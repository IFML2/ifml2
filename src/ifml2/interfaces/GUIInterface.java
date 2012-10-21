/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ifml2.interfaces;

import javax.swing.*;
import java.text.MessageFormat;

/**
 * @author realsonic
 */
public class GUIInterface extends Interface
{
    private JTextArea logTextArea = null;
    private JTextField commandTextField = null;

    public GUIInterface(JTextArea logTextArea, JTextField commandText)
    {
        this.logTextArea = logTextArea;
        this.commandTextField = commandText;
    }

    @Override
    public void outputText(String text)
    {
        logTextArea.append(text);
        logTextArea.setCaretPosition(logTextArea.getText().length());
    }

    @Override
    public String inputText()
    {
        String command = commandTextField.getText();
        commandTextField.setText("");
        outputText("\n> " + command + "\n");
        return command;
    }

    public void outputText(String text, Object ... arguments)
    {
        outputText(MessageFormat.format(text, arguments));
    }
}
