package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.om.InstructionList;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;

public class InstructionsEditor extends JDialog
{
    private static final String INSTR_EDITOR_TITLE = "Инструкции";

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList instructionsList;
    private boolean isOk;
    private InstructionList instructionListClone;

    public InstructionsEditor(Window owner, final InstructionList instructionList)
    {
        super(owner, INSTR_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // clone data
        try
        {
            instructionListClone = instructionList.clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // init form
        instructionsList.setModel(new ListModel()
        {
            @Override
            public int getSize()
            {
                return instructionList.getSize();
            }

            @Override
            public Object getElementAt(int index)
            {
                return instructionList.getInstructions().get(index);
            }

            @Override
            public void addListDataListener(ListDataListener l)
            {
                //todo addListDataListener
            }

            @Override
            public void removeListDataListener(ListDataListener l)
            {
                //todo removeListDataListener
            }
        });
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

    public void getData(InstructionList instructionList)
    {
        //todo instructionList.setInstuctions(ins);
    }
}
