package ifml2.editor.gui.instructions;

import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.ReturnInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ReturnInstrEditor extends AbstractInstrEditor {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField returnExpressionText;

    public ReturnInstrEditor(Window owner, ReturnInstruction instruction) {
        super(owner);
        initializeEditor(Instruction.getTitleFor(ReturnInstruction.class), contentPane, buttonOK, buttonCancel);

        // -- init data --
        returnExpressionText.setText(instruction.getValue());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return ReturnInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        updateData(instruction);

        ReturnInstruction returnInstruction = (ReturnInstruction) instruction;

        returnInstruction.setValue(returnExpressionText.getText());
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
