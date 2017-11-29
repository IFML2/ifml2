package ifml2.vm.instructions;

import ca.odell.glazedlists.BasicEventList;
import ifml2.IFML2Exception;
import ifml2.IFMLEntity;
import ifml2.editor.gui.instructions.*;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;
import ifml2.vm.VirtualMachine;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlTransient;
import java.text.MessageFormat;
import java.util.List;

public abstract class Instruction implements Cloneable
{
    /*@XmlAttribute(name = "position")
    public int position;*/
    @XmlTransient
    public VirtualMachine virtualMachine; // links

    private static void validateParameterForNull(String parameterValue, String instructionTitle,
            Object parameterName) throws IFML2VMException
    {
        if (parameterValue == null || "".equals(parameterValue))
        {
            throw new IFML2VMException("Параметр {0} не задан у инструкции [{1}]", parameterName, instructionTitle);
        }
    }

    protected static <T> List<T> convertToClassedList(List<? extends IFMLEntity> unknownList,
            Class<T> convertingClass) throws IFML2VMException
    {
        List<T> ifmlObjects = new BasicEventList<>();
        for (Object obj : unknownList)
        {
            if (convertingClass.isInstance(obj))
            {
                ifmlObjects.add(convertingClass.cast(obj));
            }
            else
            {
                throw new IFML2VMException("Элемент коллекции \"{0}\" - не типа Объект.", obj);
            }
        }
        return ifmlObjects;
    }

    public static String getTitleFor(Class<? extends Instruction> instrClass)
    {
        IFML2Instruction annotation = instrClass.getAnnotation(IFML2Instruction.class);
        return annotation != null ? annotation.title() : instrClass.getSimpleName();
    }

    @Override
    public Instruction clone() throws CloneNotSupportedException
    {
        return (Instruction) super.clone(); // all fields are copied by default
    }

    abstract public void run(RunningContext runningContext) throws IFML2Exception;

    protected IFMLObject getObjectFromExpression(String expression, RunningContext runningContext, String instructionTitle,
            Object parameterName, boolean objectCanBeNull) throws IFML2Exception
    {
        validateParameterForNull(expression, instructionTitle, parameterName);

        Value itemValue = ExpressionCalculator.calculate(runningContext, expression);

        if (!(itemValue instanceof ObjectValue))
        {
            throw new IFML2VMException("Тип выражения ({0}) – не Объект у инструкции [{1}]", expression, instructionTitle);
        }

        IFMLObject object = ((ObjectValue) itemValue).getValue();

        // test for null
        if (!objectCanBeNull && object == null)
        {
            throw new IFML2VMException("Объект " + expression + " не найден");
        }

        return object;
    }

    protected Item getItemFromExpression(String expression, RunningContext runningContext, String instructionTitle, Object parameterName,
            boolean objectCanBeNull) throws IFML2Exception
    {
        IFMLObject object = getObjectFromExpression(expression, runningContext, instructionTitle, parameterName, objectCanBeNull);

        if (objectCanBeNull && object == null)
        {
            return null;
        }

        if (!(object instanceof Item))
        {
            throw new IFML2VMException("Тип выражения ({0}) – не Предмет у инструкции {1}", expression, instructionTitle);
        }

        return (Item) object;
    }

    protected Location getLocationFromExpression(String expression, RunningContext runningContext, String instructionTitle,
            Object parameterName, boolean objectCanBeNull) throws IFML2Exception
    {
        IFMLObject object = getObjectFromExpression(expression, runningContext, instructionTitle, parameterName, objectCanBeNull);

        if (objectCanBeNull && object == null)
        {
            return null;
        }

        if (!(object instanceof Location))
        {
            throw new IFML2VMException("Тип выражения ({0}) – не Локация у инструкции {1}", expression, instructionTitle);
        }

        return (Location) object;
    }

    boolean getBooleanFromExpression(String expression, RunningContext runningContext, String instructionTitle,
            Object parameterName) throws IFML2Exception
    {
        validateParameterForNull(expression, instructionTitle, parameterName);

        Value boolValue = ExpressionCalculator.calculate(runningContext, expression);

        if (!(boolValue instanceof BooleanValue))
        {
            throw new IFML2VMException("Тип выражения ({0}) – не Логическое у инструкции [{1}]", expression, instructionTitle);
        }

        return ((BooleanValue) boolValue).getValue();
    }

    protected List<? extends IFMLEntity> getCollectionFromExpression(String expression, RunningContext runningContext,
            String instructionTitle, Object parameterName) throws IFML2Exception
    {
        validateParameterForNull(expression, instructionTitle, parameterName);

        Value collectionValue = ExpressionCalculator.calculate(runningContext, expression);

        if (!(collectionValue instanceof CollectionValue))
        {
            throw new IFML2VMException("Тип выражения ({0}) – не Коллекция у инструкции [{1}]", expression, instructionTitle);
        }

        return ((CollectionValue) collectionValue).getValue();
    }

    protected String getTitle()
    {
        Class<? extends Instruction> aClass = this.getClass();
        return getTitleFor(aClass);
    }

    public enum Type
    {
        SHOW_MESSAGE(ShowMessageInstr.class, ShowMessageInstrEditor.class),
        GO_TO_LOCATION(GoToLocInstruction.class, GoToLocInstrEditor.class),
        IF(IfInstruction.class, IfInstrEditor.class),
        LOOP(LoopInstruction.class, null), //todo LoopInstruction Editor
        SET_VAR(SetVarInstruction.class, SetVarInstrEditor.class),
        MOVE_ITEM(MoveItemInstruction.class, MoveItemInstrEditor.class),
        ROLL_DICE(RollDiceInstruction.class, RollDiceInstrEditor.class),
        SET_PROPERTY(SetPropertyInstruction.class, null), // todo SetPropertyInstruction Editor
        RUN_PROCEDURE(RunProcedureInstruction.class, RunProcedureInstrEditor.class),
        RETURN(ReturnInstruction.class, ReturnInstrEditor.class),
        SHOW_PICTURE(ShowPictureInstruction.class, ShowPictureInstrEditor.class);

        private Class<? extends Instruction> instrClass;
        private String title;
        private Class<? extends AbstractInstrEditor> editorClass;

        Type(@NotNull Class<? extends Instruction> instrClass, Class<? extends AbstractInstrEditor> editorClass)
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
         * @return Type item.
         * @throws IFML2Exception if there is no enum item for specified class.
         */
        public static Type getItemByClass(@NotNull Class<? extends Instruction> instructionClass) throws IFML2Exception
        {
            for (Type type : Type.values())
            {
                if (type.instrClass.equals(instructionClass))
                {
                    return type;
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
}
