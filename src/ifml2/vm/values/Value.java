package ifml2.vm.values;

public abstract class Value
{
    public abstract String getTypeName();

    public abstract String toLiteral();

    public enum OperationEnum
    {
        ADD("сложение");

        final String caption;

        OperationEnum(String caption)
        {
            this.caption = caption;
        }

        @Override
        public String toString()
        {
            return caption;
        }
    }
}
