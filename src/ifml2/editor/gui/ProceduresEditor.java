package ifml2.editor.gui;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.om.Procedure;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.InstructionTypeEnum;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class ProceduresEditor extends JDialog
{
    public static final String PROCEDURES_EDITOR_TITLE = "Процедуры";
    private JPanel contentPane;
    private JButton buttonOK;
    private JList proceduresList;
    private JList instructionsList;
    private JButton addProcedureButton;
    private JButton delProcedureButton;
    private JButton addInstructionButton;
    private JButton editInstructionButton;
    private JButton delInstructionButton;

    private HashMap<String, Procedure> procedures = null; // todo rewrite using transactional model

    private boolean isOk = false;

    private final AbstractAction delProcedureAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();

            if(procedure == null)
            {
                return;
            }

            int answer = JOptionPane.showConfirmDialog(ProceduresEditor.this, "Вы действительно хотите удалить процедуру " + procedure.getName() + "?",
                    "Удаление процедуры", JOptionPane.YES_NO_OPTION);

            if(answer == 0)
            {
                procedures.values().remove(procedure);
                updateAllData();
            }
        }
    };
    private final AbstractAction addInstructionAction = new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();

            if (procedure != null)
            {
                InstructionTypeEnum answer = EditorUtils.askInstructionType(ProceduresEditor.this);

                if (answer != null)
                {
                    try
                    {
                        Instruction instruction = answer.getInstrClass().newInstance();
                        if (EditorUtils.showAssociatedEditor(ProceduresEditor.this, instruction))
                        {
                            procedure.getInstructions().add(instruction);
                            updateSelectedProcedure();
                            instructionsList.setSelectedValue(instruction, true);
                        }
                    }
                    catch (Throwable ex)
                    {
                        GUIUtils.showErrorMessage(ProceduresEditor.this, ex);
                    }
                }
            }
        }
    };
    private final AbstractAction editInstructionAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Instruction instruction = (Instruction) instructionsList.getSelectedValue();

            if (instruction != null && EditorUtils.showAssociatedEditor(ProceduresEditor.this, instruction))
            {
                updateSelectedProcedure();
            }
        }
    };
    private final AbstractAction delInstructionAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();
            Instruction instruction = (Instruction) instructionsList.getSelectedValue();

            if (procedure != null && instruction != null &&
                    JOptionPane.showConfirmDialog(ProceduresEditor.this, "Вы действительно хотите удалить выбраную инструкцию?",
                    "Удаление инструкции", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                procedure.getInstructions().remove(instruction);
            }
        }
    };

    public ProceduresEditor(Window owner, HashMap<String, Procedure> procedures)
    {
        super(owner, PROCEDURES_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        // call onOK() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onOK();
            }
        });

        // call onOK() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addProcedureButton.setAction(new AbstractAction("Новая...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String procedureName = JOptionPane.showInputDialog(ProceduresEditor.this, "Имя новой процедуры:",
                        "Новая процедура", JOptionPane.QUESTION_MESSAGE);

                if(procedureName != null && !"".equals(procedureName))
                {
                    Procedure procedure = new Procedure(procedureName);
                    ProceduresEditor.this.procedures.put(procedureName, procedure);
                    updateAllData();
                    proceduresList.setSelectedValue(procedure, true);
                }
            }
        });
        delProcedureButton.setAction(delProcedureAction);

        addInstructionButton.setAction(addInstructionAction);
        editInstructionButton.setAction(editInstructionAction);
        delInstructionButton.setAction(delInstructionAction); 

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });

        proceduresList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateActions();
                updateSelectedProcedure();
            }
        });

        instructionsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateActions();
            }
        });

        updateActions();

        // set data in form

        this.procedures = procedures;
        updateAllData();
    }

    private void updateActions()
    {
        boolean proceduresCanBeEdited = proceduresList.getSelectedValue() != null;
        delProcedureAction.setEnabled(proceduresCanBeEdited);

        boolean instructionsCanBeEdited = instructionsList.getSelectedValue() != null;
        addInstructionAction.setEnabled(proceduresCanBeEdited);
        editInstructionAction.setEnabled(instructionsCanBeEdited);
        delInstructionAction.setEnabled(instructionsCanBeEdited);
    }

    private void updateSelectedProcedure()
    {
        Procedure procedure = (Procedure) proceduresList.getSelectedValue();

        if (procedure != null)
        {
            instructionsList.setModel(new DefaultEventListModel<Instruction>(procedure.getInstructions()));
        }
    }

    private void onOK()
    {
        isOk = true;
        dispose();
    }

    private void updateAllData()
    {
        DefaultListModel proceduresListModel = new DefaultListModel();
        for(Procedure procedure : procedures.values())
        {
            proceduresListModel.addElement(procedure);
        }
        proceduresList.setModel(proceduresListModel);
    }

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
