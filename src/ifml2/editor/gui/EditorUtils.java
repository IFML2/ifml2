package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.editor.gui.instructions.IfInstrEditor;
import ifml2.editor.gui.instructions.ShowMessageInstrEditor;
import ifml2.vm.instructions.IfInstruction;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.InstructionTypeEnum;
import ifml2.vm.instructions.ShowMessageInstr;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class EditorUtils
{
    public static InstructionTypeEnum askInstructionType(Dialog owner)
    {
        return (InstructionTypeEnum) JOptionPane.showInputDialog(owner, "Выберите тип инструкции",
                "Новая инструкция", JOptionPane.QUESTION_MESSAGE, null, InstructionTypeEnum.values(),
                InstructionTypeEnum.SHOW_MESSAGE);
    }

    /**
     * Shows editor associated for given instruction and update it if closed by Ok button.
     * @param owner Dialog-owner of showing editor
     * @param instruction Instruction to edit
     * @return true if Ok button was pressed and instruction was updated and false in other case
     */
    public static boolean showAssociatedEditor(Dialog owner, @NotNull Instruction instruction)
    {
        try
        {
            InstructionTypeEnum instrType = InstructionTypeEnum.getItemByClass(instruction.getClass());

            switch (instrType)
            {
                case SHOW_MESSAGE:
                    ShowMessageInstr showMessageInstr = (ShowMessageInstr) instruction;
                    ShowMessageInstrEditor showMessageInstrEditor = new ShowMessageInstrEditor(owner, showMessageInstr);
                    if(showMessageInstrEditor.showDialog())
                    {
                        showMessageInstrEditor.getData(showMessageInstr);
                        return true;
                    }
                    break;
                case IF:
                    IfInstruction ifInstruction = (IfInstruction) instruction;
                    IfInstrEditor ifInstrEditor = new IfInstrEditor(owner, ifInstruction);
                    if(ifInstrEditor.showDialog())
                    {
                        ifInstrEditor.getData(ifInstruction);
                        return true;
                    }
                    break;
                default:
                    JOptionPane.showMessageDialog(owner, "Инструкция " + instruction.getClass().getSimpleName() + " пока не редактируется");
            }
        }
        catch (Exception e)
        {
            GUIUtils.showErrorMessage(owner, e);
        }

        return false;
    }
}
