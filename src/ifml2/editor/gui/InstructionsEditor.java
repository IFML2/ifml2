package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.om.InstructionList;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.InstructionTypeEnum;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

public class InstructionsEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList instructionsList;
    private JButton addInstrButton;
    private JButton editInstrButton;
    private JButton delInstrButton;
    private JButton upButton;
    private JButton downButton;
    private boolean isOk;

    private static final String INSTR_EDITOR_TITLE = "Инструкции";

    // data to clone
    private InstructionList instructionListClone;

    private final AbstractAction editInstrAction = new AbstractAction("Изменить...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Instruction selectedInstr = (Instruction) instructionsList.getSelectedValue();
            if(selectedInstr != null)
            {
                EditorUtils.showAssociatedEditor(InstructionsEditor.this, selectedInstr);
            }
        }
    };
    private final AbstractAction delInstrAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if(JOptionPane.showConfirmDialog(InstructionsEditor.this, "Вы действительно хотите удалить выбранную инструкцию?",
                    "Удаление инструкции", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
            {
                Instruction selectedInstr = (Instruction) instructionsList.getSelectedValue();
                if(selectedInstr != null)
                {
                    instructionListClone.getInstructions().remove(selectedInstr);
                }
            }
        }
    };
    private final AbstractAction upAction = new AbstractAction("", GUIUtils.UP_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            int selIdx = instructionsList.getSelectedIndex();
            if(selIdx > 0)
            {
                Collections.swap(instructionListClone.getInstructions(), selIdx, selIdx - 1);
                instructionsList.setSelectedIndex(selIdx - 1);
            }
        }
    };
    private final AbstractAction downAction = new AbstractAction("", GUIUtils.DOWN_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            int selIdx = instructionsList.getSelectedIndex();
            EventList<Instruction> instructions = instructionListClone.getInstructions();
            if(selIdx < instructions.size() - 1)
            {
                Collections.swap(instructions, selIdx, selIdx + 1);
                instructionsList.setSelectedIndex(selIdx + 1);
            }
        }
    };

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

        // -- form actions init --

        addInstrButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionTypeEnum instrType = EditorUtils.askInstructionType(InstructionsEditor.this);
                if(instrType != null)
                {
                    try
                    {
                        Instruction instruction = instrType.getInstrClass().newInstance();
                        if (EditorUtils.showAssociatedEditor(InstructionsEditor.this, instruction))
                        {
                            instructionListClone.getInstructions().add(instruction);
                            instructionsList.setSelectedValue(instruction, true);
                        }
                    }
                    catch (Throwable ex)
                    {
                        GUIUtils.showErrorMessage(InstructionsEditor.this, ex);
                    }
                }
            }
        });
        editInstrButton.setAction(editInstrAction);
        delInstrButton.setAction(delInstrAction);
        upButton.setAction(upAction);
        downButton.setAction(downAction);

        // -- clone data --

        try
        {
            instructionListClone = instructionList.clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // -- init form --

        instructionsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                UpdateActions();
            }
        });
        instructionsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    Instruction instruction = (Instruction) instructionsList.getSelectedValue();
                    if(instruction != null)
                    {
                        EditorUtils.showAssociatedEditor(InstructionsEditor.this, instruction);
                    }
                }
            }
        });
        instructionsList.setModel(new DefaultEventListModel<Instruction>(instructionListClone.getInstructions()));

        UpdateActions();
    }

    private void UpdateActions()
    {
        boolean isInstrSelected = instructionsList.getSelectedValue() != null;
        editInstrAction.setEnabled(isInstrSelected);
        delInstrAction.setEnabled(isInstrSelected);

        int selectedInstrIdx = instructionsList.getSelectedIndex();
        upAction.setEnabled(isInstrSelected && selectedInstrIdx > 0);
        downAction.setEnabled(isInstrSelected && selectedInstrIdx < instructionsList.getModel().getSize() - 1);
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

    public void getData(@NotNull InstructionList instructionList)
    {
        instructionList.setInstructions(instructionListClone.getInstructions());
    }
}
