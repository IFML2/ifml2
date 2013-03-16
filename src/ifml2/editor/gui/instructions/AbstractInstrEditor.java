package ifml2.editor.gui.instructions;

import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;

/**
 * Common ancestor for Instruction editors.
 * Auto reacts to OK, Cancel and X buttons clicks.
 * To use it implement get methods to tune editor and call super() and init() in constructor.
 */
public abstract class AbstractInstrEditor extends JDialog
{
    private boolean isOk;

    public AbstractInstrEditor(Window owner)
    {
        super(owner, ModalityType.DOCUMENT_MODAL);
    }

    protected abstract JButton getButtonCancel();

    protected abstract JButton getButtonOK();

    protected abstract JPanel getEditorContentPane();

    protected abstract String getEditorTitle();

    protected abstract Class<? extends Instruction> getInstrClass();

    protected void init()
    {
        setTitle(getEditorTitle());

        JPanel editorContentPane = getEditorContentPane();
        setContentPane(editorContentPane);

        JButton buttonOK = getButtonOK();

        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        getButtonCancel().addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onCancel();}
        });

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

    /**
     * The main ancestor's method function is check Instruction class.
     * @param instruction to check
     * @throws IFML2EditorException if instruction is of wrong class.
     */
    public void getData(@NotNull Instruction instruction) throws IFML2EditorException
    {
        if (!instruction.getClass().equals(getInstrClass()))
        {
            throw new IFML2EditorException(MessageFormat.format("Instruction should be of class {0}", getInstrClass()));
        }
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
