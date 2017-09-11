package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Common abstract ancestor for all editors.<br/>
 * <b>Usage:</b>
 * Create descendant JDialog with needed type. In constructor first of all call super(owner) and  initializeEditor().
 * Implement updateData() where fill data object with data from editor.
 * Implement validateData() if you need validation.
 *
 * @param <T> Edited object type.
 */
public abstract class AbstractEditor<T> extends JDialog {
    private boolean isOk;

    public AbstractEditor(Window owner) {
        super(owner, ModalityType.DOCUMENT_MODAL);
    }

    /**
     * Initialize editor.
     *
     * @param editorTitle       editor title.
     * @param editorContentPane editor main JPanel.
     * @param buttonOK          editor OK button.
     * @param buttonCancel      editor Cancel button.
     */
    protected void initializeEditor(String editorTitle, @NotNull JPanel editorContentPane, JButton buttonOK, JButton buttonCancel) {
        setTitle(editorTitle);

        setContentPane(editorContentPane);

        rootPane.setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        if (buttonCancel != null) {
            buttonCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });
        }

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        editorContentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Validate input date in case of pressing OK button. Returns true by default. Override for custom logic.
     * Overriding: if data is incorrect throw DataNotValidException.
     *
     * @see DataNotValidException
     */
    protected void validateData() throws DataNotValidException {
        // do nothing - everything is correct by default
    }

    /**
     * Call this method to get edited data. Custom method should write data in given variable 'data'.
     *
     * @param data object what should be filled with edited data.
     * @throws IFML2EditorException if something goes wrong.
     */
    public abstract void updateData(@NotNull T data) throws IFML2EditorException;

    private void onOK() {
        try {
            validateData();
            isOk = true;
            dispose();
        } catch (DataNotValidException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Данные не верны", JOptionPane.ERROR_MESSAGE);
            Component componentForFocus = e.getComponentForFocus();
            if (componentForFocus != null) {
                componentForFocus.requestFocusInWindow();
            }
        }
    }

    private void onCancel() {
        isOk = false;
        dispose();
    }

    /**
     * Show dialog and wait for closing by buttons (OK or Cancel) or by cross.
     *
     * @return true if OK was pressed.
     */
    public boolean showDialog() {
        setVisible(true);
        return isOk;
    }
}
