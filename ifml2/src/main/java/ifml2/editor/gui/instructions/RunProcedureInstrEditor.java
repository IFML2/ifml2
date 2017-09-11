package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.RunProcedureInstruction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RunProcedureInstrEditor extends AbstractInstrEditor
{
    private static final String RUN_PROCEDURE_EDITOR_TITLE = "Вызвать процедуру";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox procedureCombo;
    private JTextField returnToVarText;
    private JList paramsList;
    private JTextField paramValueText;
    @Nullable private EventList<Procedure.FilledParameter> filledParameters;

    public RunProcedureInstrEditor(Window owner, final RunProcedureInstruction runProcedureInstruction, Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(RUN_PROCEDURE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // load data and set dependencies
        paramsList.addListSelectionListener(new ListSelectionListener() // react on parameter selection
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                Parameter selectedParameter = (Parameter) paramsList.getSelectedValue();
                if(selectedParameter != null)
                {
                    Procedure.FilledParameter filledParameter = getFilledParameterByName(selectedParameter.getName());
                    if (filledParameter != null)
                    {
                        paramValueText.setText(filledParameter.getValueExpression());
                        paramValueText.setEnabled(true);
                    }
                }
                else
                {
                    paramValueText.setText(null);
                }
            }
        });

        procedureCombo.addActionListener(new AbstractAction() // react on procedure selection
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Procedure procedure = (Procedure) procedureCombo.getSelectedItem();
                if (procedure != null)
                {
                    EventList<Parameter> parameters = procedure.getParameters();
                    paramsList.setModel(new DefaultEventListModel<Parameter>(parameters));
                    filledParameters = new BasicEventList<Procedure.FilledParameter>(parameters.size());
                    for (Parameter parameter : parameters)
                    {
                        String name = parameter.getName();
                        Procedure.FilledParameter parameterByName = runProcedureInstruction.getParameterByName(name);
                        // if there are parameters in edited instruction then get them, else take empty
                        if (parameterByName != null)
                        {
                            filledParameters.add(new Procedure.FilledParameter(name, parameterByName.getValueExpression()));
                        }
                        else
                        {
                            filledParameters.add(new Procedure.FilledParameter(name, ""));
                        }
                    }
                }
                else
                {
                    paramsList.setModel(new DefaultEventListModel<Parameter>(new BasicEventList<Parameter>()));
                    filledParameters = null;
                }
                paramValueText.setEnabled(false);
                paramValueText.setText(null);
            }
        });
        procedureCombo.setModel(new DefaultEventComboBoxModel<Procedure>(storyDataHelper.getProcedures())); // load procedures
        procedureCombo.setSelectedItem(runProcedureInstruction.getProcedure()); // select procedure

        paramValueText.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                UpdateChange();
            }

            private void UpdateChange()
            {
                Parameter selectedParameter = (Parameter) paramsList.getSelectedValue();
                if (selectedParameter != null)
                {
                    Procedure.FilledParameter filledParameter = getFilledParameterByName(selectedParameter.getName());
                    if(filledParameter != null)
                    {
                        filledParameter.setValueExpression(paramValueText.getText());
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                UpdateChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                UpdateChange();
            }
        });

        returnToVarText.setText(runProcedureInstruction.getReturnToVar());
    }

    private Procedure.FilledParameter getFilledParameterByName(String name)
    {
        if(filledParameters != null)
        {
            for (Procedure.FilledParameter filledParameter : filledParameters)
            {
                String filledParameterName = filledParameter.getName();
                if (filledParameterName != null && filledParameter.getName().equalsIgnoreCase(name))
                {
                    return filledParameter;
                }
            }
        }

        return null;
    }

    @Override
    protected Class<? extends Instruction> getInstrClass()
    {
        return RunProcedureInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException
    {
        super.updateData(instruction);

        RunProcedureInstruction runProcedureInstruction = (RunProcedureInstruction) instruction;
        runProcedureInstruction.setProcedure((Procedure) procedureCombo.getSelectedItem());
        runProcedureInstruction.setParameters(filledParameters);
        runProcedureInstruction.setReturnToVar(returnToVarText.getText());
    }

    @Override
    protected void validateData() throws DataNotValidException
    {
        if (procedureCombo.getSelectedItem() == null)
        {
            throw new DataNotValidException("Должна быть выбрана процедура для выполнения.", procedureCombo);
        }
    }
}
