package ifml2.vm.values;

import org.jetbrains.annotations.NotNull;

/**
 * Empty value
 */
public class EmptyValue extends Value
{

    @Override
    public String getTypeName()
    {
        return "пусто";
    }

    @Override
    public String toString()
    {
        return toLiteral();
    }

    @Override
    public String toLiteral()
    {
        return "пусто";
    }

    @Override
    public ValueCompareResultEnum compareTo(@NotNull Value rightValue)
    {
        return rightValue instanceof EmptyValue ? ValueCompareResultEnum.EQUAL : ValueCompareResultEnum.UNEQUAL;
    }
}
