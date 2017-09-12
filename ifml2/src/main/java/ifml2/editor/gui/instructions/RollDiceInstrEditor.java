package ifml2.editor.gui.instructions;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.RollDiceInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class RollDiceInstrEditor extends AbstractInstrEditor {
    private static final String ROLL_DICE_EDITOR_TITLE = "Бросить кость";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JTextField varText;

    public RollDiceInstrEditor(Window owner, RollDiceInstruction instruction) {
        super(owner);
        initializeEditor(ROLL_DICE_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // initialize and load spinners
        fromSpinner.setModel(new SpinnerNumberModel(instruction.getFromNumber(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        toSpinner.setModel(new SpinnerNumberModel(instruction.getToNumber(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

        // load var name
        varText.setText(instruction.getVarName());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return RollDiceInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        RollDiceInstruction rollDiceInstruction = (RollDiceInstruction) instruction;
        rollDiceInstruction.setFromNumber(((SpinnerNumberModel) fromSpinner.getModel()).getNumber().intValue());
        rollDiceInstruction.setToNumber(((SpinnerNumberModel) toSpinner.getModel()).getNumber().intValue());
        rollDiceInstruction.setVarName(varText.getText());
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
