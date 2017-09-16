package ifml2.editor.gui.editors;

import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.InstructionList;
import ifml2.om.Restriction;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RestrictionEditor extends AbstractEditor<Restriction> {
    private static final String EDITOR_TITLE = "Ограничение";
    private InstructionList reactionClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField conditionText;
    private JButton editReactionButton;
    private JList reactionInstructionsList;

    public RestrictionEditor(Window owner, Restriction restriction, final Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // set listeners
        editReactionButton.setAction(new AbstractAction("Редактировать инструкции...", GUIUtils.EDIT_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstructionsEditor instructionsEditor = new InstructionsEditor(RestrictionEditor.this, reactionClone, storyDataHelper);
                if (instructionsEditor.showDialog()) {
                    instructionsEditor.updateData(reactionClone);
                }
            }
        });

        try {
            // clone data
            reactionClone = restriction.getReaction().clone();
            // load data
            conditionText.setText(restriction.getCondition());
            reactionInstructionsList.setModel(new DefaultEventListModel<Instruction>(reactionClone.getInstructions()));
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (conditionText.getText().trim().length() <= 0) {
            throw new DataNotValidException("Условие не может быть пустым!", conditionText);
        }
    }

    @Override
    public void updateData(@NotNull Restriction data) throws IFML2EditorException {
        data.setCondition(conditionText.getText());
        data.getReaction().replaceInstructions(reactionClone);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

}
