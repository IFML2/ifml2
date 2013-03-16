package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.editor.gui.instructions.AbstractInstrEditor;
import ifml2.editor.gui.instructions.InstructionTypeEnum;
import ifml2.vm.instructions.Instruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

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
            Class<? extends Instruction> instrClass = instrType.getInstrClass();
            Class<? extends AbstractInstrEditor> editorClass = instrType.getAssociatedEditor();
            if(editorClass != null)
            {
                AbstractInstrEditor instrEditor = editorClass.getConstructor(Window.class, instrClass).newInstance(owner, instruction);
                if(instrEditor.showDialog())
                {
                    instrEditor.getInstruction(instruction);
                    return true;
                }
            }
            else
            {
                JOptionPane.showMessageDialog(owner, MessageFormat.format("Инструкция \"{0}\" пока не редактируется",
                        instrType.getTitle()));
            }
        }
        catch (Throwable e)
        {
            GUIUtils.showErrorMessage(owner, e);
        }

        return false;
    }
}
