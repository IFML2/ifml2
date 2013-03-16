package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class AbstractEditor<T> extends JDialog
{
    private boolean isOk;

    public AbstractEditor(Window owner)
    {
        super(owner, ModalityType.DOCUMENT_MODAL);
    }

    public abstract void getData(@NotNull T data) throws IFML2EditorException;

    protected void initializeEditor(String editorTitle, @NotNull JPanel editorContentPane, JButton buttonOK, JButton buttonCancel)
    {
        setTitle(editorTitle);

        setContentPane(editorContentPane);

        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        if (buttonCancel != null)
        {
            buttonCancel.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e) {onCancel();}
            });
        }

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        editorContentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK()
    {
        isOk = true;
        dispose();
    }

    private void onCancel()
    {
        isOk = false;
        dispose();
    }

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
