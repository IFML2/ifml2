/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ifml2.interfaces;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.text.MessageFormat;

/**
 * @author realsonic
 */
public class GUIInterface extends Interface
{
    private static final Logger LOG = Logger.getLogger(GUIInterface.class);
    private JTextArea logTextArea = null;
    private JTextField commandTextField = null;
    private JScrollPane scrollPane;

    public GUIInterface(@NotNull JTextArea logTextArea, @NotNull JTextField commandText, @NotNull JScrollPane scrollPane)
    {
        this.logTextArea = logTextArea;
        this.commandTextField = commandText;
        this.scrollPane = scrollPane;

        //logTextArea.addLis
    }

    @Override
    public void outputText(String text)
    {
        logTextArea.append(text);
    }

    @Override
    public String inputText()
    {
        String command = commandTextField.getText();
        commandTextField.setText("");
        outputText("\n");

        Rectangle startLocation;
        int lastLine = logTextArea.getLineCount() - 1;
        try
        {
            startLocation = logTextArea.modelToView(logTextArea.getLineStartOffset(lastLine));
        }
        catch (BadLocationException e)
        {
            LOG.error("Error while scrolling JTextArea", e);
            throw new RuntimeException(e);
        }

        outputText("> " + command + "\n");

        final Point viewPosition = new Point(startLocation.x, startLocation.y);
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                scrollPane.getViewport().setViewPosition(viewPosition);
            }
        });

        return command;
    }

    public void outputText(String text, Object... arguments)
    {
        outputText(MessageFormat.format(text, arguments));
    }
}
