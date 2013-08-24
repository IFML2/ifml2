package ifml2.editor.gui;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.om.InstructionList;
import ifml2.om.Restriction;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RestrictionEditor extends AbstractEditor<Restriction>
{
    private static final String EDITOR_TITLE = "Ограничение";
    private InstructionList reactionClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField conditionText;
    private JButton editReactionButton;
    private JList reactionInstructionsList;

    @Override
    protected void validateData() throws DataNotValidException
    {
        if (conditionText.getText().trim().length() <= 0)
        {
            throw new DataNotValidException("Условие не может быть пустым!", conditionText);
        }
    }

    public RestrictionEditor(Window owner, Restriction restriction)
    {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // set listeners
        editReactionButton.setAction(new AbstractAction("Редактировать инструкции...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InstructionsEditor instructionsEditor = new InstructionsEditor(RestrictionEditor.this, reactionClone);
                if(instructionsEditor.showDialog())
                {
                    instructionsEditor.getData(reactionClone);
                }
            }
        });

        try
        {
            // clone data
            reactionClone = restriction.getReaction().clone();
            // load data
            conditionText.setText(restriction.getCondition());
            reactionInstructionsList.setModel(new DefaultEventListModel<Instruction>(reactionClone.getInstructions()));
        }
        catch (CloneNotSupportedException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }
    }

    @Override
    public void getData(@NotNull Restriction data) throws IFML2EditorException
    {
        data.setCondition(conditionText.getText());
        data.getReaction().rewriteInstructions(reactionClone);
    }
}
