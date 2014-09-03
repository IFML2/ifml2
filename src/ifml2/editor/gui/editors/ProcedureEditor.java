package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ProcedureEditor extends AbstractEditor<Procedure>
{
    private static final String PROCEDURE_EDITOR_TITLE = "Процедура";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JList parametersList;
    private JList instructionsList;
    private JButton addParameterButton;
    private JButton editParamaterButton;
    private JButton deleteParameterButton;
    private JButton addInstructionButton;
    private JButton editInstructionButton;
    private JButton deleteInstructionButton;

    public ProcedureEditor(Window owner, @NotNull final Procedure procedure, Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(PROCEDURE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // init form
        nameText.setText(procedure.getName());
        parametersList.setModel(new DefaultEventListModel<Parameter>(procedure.getParameters()));
        instructionsList.setModel(new DefaultEventListModel<Instruction>(procedure.getInstructions()));

        // init buttons
        addParameterButton.setAction(new ButtonAction(addParameterButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название нового параметра:",
                                                                   "Новый параметр", JOptionPane.QUESTION_MESSAGE);

                if(parameterName != null && !"".equals(parameterName))
                {
                    Parameter parameter = new Parameter(parameterName);
                    procedure.getParameters().add(parameter);
                    parametersList.setSelectedValue(parameter, true);
                }

            }
        });
        editParamaterButton.setAction(new ButtonAction(editParamaterButton, parametersList)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Parameter parameter = (Parameter) parametersList.getSelectedValue();
                if(parameter != null)
                {
                    String parameterName = parameter.getName();
                    parameterName = JOptionPane.showInputDialog(ProcedureEditor.this, "Название параметра:", parameterName);
                    if(parameterName != null && !"".equals(parameterName))
                    {
                        parameter.setName(parameterName);
                        parametersList.setSelectedValue(parameter, true);
                    }
                }
            }

            /*@Override
            public void registerListeners()
            {
                parametersList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!parametersList.isSelectionEmpty()); // depends on selection
                    }
                });
            }*/
        });
    }

    @Override
    public void getData(@NotNull Procedure data) throws IFML2EditorException
    {
        //todo
    }
}
