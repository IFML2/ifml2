package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.om.InstructionList;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.*;

public class InstructionsEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTree instructionsTree;

    public InstructionsEditor(InstructionList instructionList)
    {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

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
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

        // init tree
        instructionsTree.setModel(new TreeModel()
        {
            @Override
            public Object getRoot()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Object getChild(Object parent, int index)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getChildCount(Object parent)
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isLeaf(Object node)
            {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void valueForPathChanged(TreePath path, Object newValue)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getIndexOfChild(Object parent, Object child)
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addTreeModelListener(TreeModelListener l)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void removeTreeModelListener(TreeModelListener l)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    private void onOK()
    {
// add your code here
        dispose();
    }

    private void onCancel()
    {
// add your code here if necessary
        dispose();
    }
}
