package ifml2.editor.gui.instructions;

import ifml2.IFML2Exception;
import ifml2.vm.instructions.*;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public enum InstructionTypeEnum
{
    SHOW_MESSAGE(ShowMessageInstr.class, ShowMessageInstrEditor.class),
    GO_TO_LOCATION(GoToLocInstruction.class, GoToLocInstrEditor.class),
    IF(IfInstruction.class, IfInstrEditor.class),
    LOOP(LoopInstruction.class, null), //todo LoopInstruction Editor
    SET_VAR(SetVarInstruction.class, SetVarInstrEditor.class),
    MOVE_ITEM(MoveItemInstruction.class, MoveItemInstrEditor.class),
    ROLL_DICE(RollDiceInstruction.class, RollDiceInstrEditor.class),
    SET_PROPERTY(SetPropertyInstruction.class, null); // todo SetPropertyInstruction Editor
    private Class<? extends Instruction> instrClass;
    private String title;
    private Class<? extends AbstractInstrEditor> editorClass;

    InstructionTypeEnum(@NotNull Class<? extends Instruction> instrClass, Class<? extends AbstractInstrEditor> editorClass)
    {
        this.instrClass = instrClass;
        this.editorClass = editorClass;

        try
        {
            IFML2Instruction annotation = instrClass.getAnnotation(IFML2Instruction.class);
            this.title = annotation != null ? annotation.title() : instrClass.getSimpleName();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns enum item by Instruction subclass.
     *
     * @param instructionClass instruction subclass.
     * @return InstructionTypeEnum item.
     * @throws IFML2Exception if there is no enum item for specified class.
     */
    public static InstructionTypeEnum getItemByClass(@NotNull Class<? extends Instruction> instructionClass) throws IFML2Exception
    {
        for (InstructionTypeEnum instructionTypeEnum : values())
        {
            if (instructionTypeEnum.instrClass.equals(instructionClass))
            {
                return instructionTypeEnum;
            }
        }

        throw new IFML2Exception(MessageFormat.format("No enum element associated with class {0}", instructionClass));
    }

    @Override
    public String toString()
    {
        return title;
    }

    public Class<? extends Instruction> getInstrClass()
    {
        return instrClass;
    }

    public Instruction createInstrInstance() throws IllegalAccessException, InstantiationException
    {
        return instrClass.newInstance();
    }

    public Class<? extends AbstractInstrEditor> getAssociatedEditor()
    {
        return editorClass;
    }

    public String getTitle()
    {
        return title;
    }
}
