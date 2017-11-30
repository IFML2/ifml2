package ifml2.parser;

import ifml2.om.IFMLObject;

public class FormalElement
{
    public final Type type;
    public IFMLObject object = null;
    private String literal = "";
    private String parameterName = "";

    public FormalElement(String formalElement, String parameter)
    {
        this.type = Type.LITERAL;
        this.literal = formalElement;
        this.parameterName = parameter;
    }

    public FormalElement(IFMLObject object, String parameter)
    {
        this.type = Type.OBJECT;
        this.object = object;
        this.parameterName = parameter;
    }

    public String getParameterName()
    {
        return parameterName;
    }

    public String getLiteral()
    {
        return literal;
    }

    public Type getType()
    {
        return type;
    }

    public IFMLObject getObject()
    {
        return object;
    }

	@Override
	public String toString()
	{
		switch (type)
		{
			case LITERAL:
				return literal;

			case OBJECT:
				return object.toString();

			default:
				throw new RuntimeException("Неверное значение типа формального элемента в FormalElement.toString()!");
		}
	}

    public enum Type
    {
        LITERAL,
        OBJECT
    }
}
