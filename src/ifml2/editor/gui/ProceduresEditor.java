package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.editor.gui.instructions.ShowMessageInstrEditor;
import ifml2.om.Procedure;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.InstructionTypeEnum;
import ifml2.vm.instructions.ShowMessageInstr;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class ProceduresEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JList proceduresList;
    private JList instructionsList;
    private JButton addProcedureButton;
    private JButton delProcedureButton;
    private JButton addInstructionButton;
    private JButton editInstructionButton;
    private JButton delInstructionButton;

    private final JDialog dialog;
    private HashMap<String, Procedure> procedures = null;

    private final DelProcedureAction delProcedureAction = new DelProcedureAction();

    private final AddInstructionAction addInstructionAction = new AddInstructionAction();
    private final EditInstructionAction editInstructionAction = new EditInstructionAction();
    private final DelInstructionAction delInstructionAction = new DelInstructionAction();

    public ProceduresEditor()
    {
        dialog = this;

        setTitle("Процедуры");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(dialog);

        AddProcedureAction addProcedureAction = new AddProcedureAction();
        addProcedureButton.setAction(addProcedureAction);
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

        if(procedure == null)
        {
            return;
        }

        DefaultListModel instructionsListModel = new DefaultListModel();
        for(Instruction instruction : procedure.getInstructions())
        {
            instructionsListModel.addElement(instruction);
        }
        instructionsList.setModel(instructionsListModel);
    }

    private void onOK()
    {
        dispose();
    }

    public void setAllData(HashMap<String, Procedure> procedures)
    {
        this.procedures = procedures;
        updateAllData();
    }

    private class AddProcedureAction extends AbstractAction
    {
        private AddProcedureAction()
        {
            super("Новая...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String procedureName = JOptionPane.showInputDialog(dialog, "Имя новой процедуры:");

            if(procedureName != null && !"".equals(procedureName))
            {
                Procedure procedure = new Procedure(procedureName);
                procedures.put(procedureName, procedure);
                updateAllData();
                proceduresList.setSelectedValue(procedure, true);
            }
        }
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

    private class DelProcedureAction extends AbstractAction
    {
        private DelProcedureAction()
        {
            super("Удалить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();

            if(procedure == null)
            {
                return;
            }

            int answer = JOptionPane.showConfirmDialog(dialog, "Вы действительно хотите удалить процедуру " + procedure.getName() + "?",
                    "Удаление процедуры", JOptionPane.YES_NO_OPTION);

            if(answer == 0)
            {
                procedures.values().remove(procedure);
                updateAllData();
            }
        }
    }

    private class EditInstructionAction extends AbstractAction
    {
        private EditInstructionAction()
        {
            super("Редактировать...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Instruction instruction = (Instruction) instructionsList.getSelectedValue();

            if(instruction == null)
            {
                return;
            }

            if(showAssociatedEditor(instruction))
            {
                updateSelectedProcedure();
            }
        }
    }

    private boolean showAssociatedEditor(Instruction instruction)
    {
        if(instruction == null)
        {
            return false;
        }

        if(instruction instanceof ShowMessageInstr)
        {
            ShowMessageInstrEditor showMessageInstrEditor = new ShowMessageInstrEditor();
            showMessageInstrEditor.setData((ShowMessageInstr) instruction);
            showMessageInstrEditor.setVisible(true);
            if(showMessageInstrEditor.isOk)
            {
                showMessageInstrEditor.getData((ShowMessageInstr) instruction);
                return true;
            }
        }
        else
        {
            JOptionPane.showMessageDialog(dialog, "Инструкция для " + instruction.getClass().getName() + " пока не редактируется");
        }

        return false;
    }

    private class DelInstructionAction extends AbstractAction
    {
        private DelInstructionAction()
        {
            super("Удалить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();
            Instruction instruction = (Instruction) instructionsList.getSelectedValue();

            if(procedure == null || instruction == null)
            {
                return;
            }

            int answer = JOptionPane.showConfirmDialog(dialog, "Вы действительно хотите удалить выбраную инструкцию?",
                    "Удаление инструкции", JOptionPane.YES_NO_OPTION);

            if(answer == 0)
            {
                procedure.getInstructions().remove(instruction);
                updateSelectedProcedure();
            }
        }
    }

    private class AddInstructionAction extends AbstractAction
    {
        private AddInstructionAction()
        {
            super("Добавить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Procedure procedure = (Procedure) proceduresList.getSelectedValue();

            if(procedure == null)
            {
                return;
            }

            InstructionTypeEnum answer = (InstructionTypeEnum) JOptionPane.showInputDialog(dialog, "Выберите тип инструкции",
                    "Новая инструкция", JOptionPane.PLAIN_MESSAGE, null, InstructionTypeEnum.values(),
                    InstructionTypeEnum.SHOW_MESSAGE);

            try
            {
                Instruction instruction = (Instruction) answer.getInstrClass().newInstance();
                if(showAssociatedEditor(instruction))
                {
                    procedure.getInstructions().add(instruction);
                    updateSelectedProcedure();
                    instructionsList.setSelectedValue(instruction, true);
                }
            }
            catch (Throwable ex)
            {
                GUIUtils.showErrorMessage(dialog, ex);
            }
        }
    }
}
