package ifml2.vm.instructions;

@SuppressWarnings({"UnusedDeclaration"})
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

    private Class instrClass;
    private String title;

    InstructionTypeEnum(Class instrClass/*, String title*/)
    {
        this.instrClass = instrClass;

        try
        {
           this.title = (String) instrClass.getMethod("getTitle").invoke(instrClass);
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

    public Class getInstrClass()
    {
        return instrClass;
    }
}
