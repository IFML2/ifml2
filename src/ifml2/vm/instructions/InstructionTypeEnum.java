package ifml2.vm.instructions;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public enum InstructionTypeEnum
{
    SHOW_LOC_NAME (ShowLocNameInstruction.class),
    SHOW_ITEM_DESC (ShowItemDescInstruction.class),
    SHOW_INVENTORY (ShowInventoryInstruction.class),
    SHOW_MESSAGE (ShowMessageInstr.class),
    GO_TO_LOCATION (GoToLocInstruction.class),
    GET_ITEM (GetItemInstruction.class),
    IF (IfInstruction.class),
    LOOP (LoopInstruction.class);

    private Class<? extends Instruction> instrClass;
    private String title;

    InstructionTypeEnum(@NotNull Class<? extends Instruction> instrClass)
    {
        this.instrClass = instrClass;

        try
        {
           this.title = (String) instrClass.getMethod("getTitle").invoke(instrClass);
        }
        catch (NoSuchMethodException e)
        {
            throw new InternalError(MessageFormat.format("{0} class hasn't getTitle method!", instrClass.getSimpleName()));
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }

        //this.title = title;
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

    public static InstructionTypeEnum getItemByClass(@NotNull Class<? extends Instruction> instructionClass)
    {
        for (InstructionTypeEnum instructionTypeEnum : values())
        {
            if(instructionTypeEnum.instrClass.equals(instructionClass))
            {
                return instructionTypeEnum;
            }
        }

        throw new InternalError(MessageFormat.format("No enum element associated with class {0}", instructionClass));
    }
}
