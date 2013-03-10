package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.InstructionsEditor;
import ifml2.om.InstructionList;
import ifml2.vm.instructions.IfInstruction;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IfInstrEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField conditionText;
    private JButton editThenButton;
    private JButton editElseButton;
    private JList thenInstructionsList;
    private JList elseInstructionsList;

    private static final String IF_INSTR_EDITOR_TITLE = "Проверка условия";
    private InstructionList thenInstructionsClone;
    private InstructionList elseInstructionsClone;
    private boolean isOk;

    public IfInstrEditor(Window owner, @NotNull IfInstruction instruction)
    {
        super(owner, IF_INSTR_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        buttonCancel.addActionListener(new ActionListener()
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
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        MouseAdapter mouseAdapter = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    JList source = (JList) e.getSource();
                    Instruction instruction = (Instruction) source.getSelectedValue();
                    if (instruction != null)
                    {
                        EditorUtils.showAssociatedEditor(IfInstrEditor.this, instruction);
                    }
                }
            }
        };
        thenInstructionsList.addMouseListener(mouseAdapter);
        elseInstructionsList.addMouseListener(mouseAdapter);

        // -- init actions --
        editThenButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionsEditor instructionsEditor = new InstructionsEditor(IfInstrEditor.this, thenInstructionsClone);
                if(instructionsEditor.showDialog())
                {
                    instructionsEditor.getData(thenInstructionsClone);
                }
            }
        });
        editElseButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionsEditor instructionsEditor = new InstructionsEditor(IfInstrEditor.this, elseInstructionsClone);
                if(instructionsEditor.showDialog())
                {
                    instructionsEditor.getData(elseInstructionsClone);
                }
            }
        });

        // -- clone data --
        try
        {
            thenInstructionsClone = instruction.getThenInstructions().clone();
            elseInstructionsClone = instruction.getElseInstructions().clone();
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        // -- init form --
        conditionText.setText(instruction.getCondition());
        thenInstructionsList.setModel(new DefaultEventListModel<Instruction>(thenInstructionsClone.getInstructions()));
        elseInstructionsList.setModel(new DefaultEventListModel<Instruction>(elseInstructionsClone.getInstructions()));
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

    public void getData(IfInstruction ifInstruction)
    {
        ifInstruction.setCondition(conditionText.getText());
        ifInstruction.setThenInstructions(thenInstructionsClone);
        ifInstruction.setElseInstructions(elseInstructionsClone);
    }
}
