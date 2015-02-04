package ifml2.parser;

import ifml2.om.IFMLObject;

public class FormalElement
{
    public String getParameterName()
    {
        return parameterName;
    }

    public String getLiteral()
    {
        return literal;
    }

    public enum FormalElementTypeEnum
	{
		LITERAL,
		OBJECT
    }

    public final FormalElementTypeEnum type;
    public FormalElementTypeEnum getType()
    {
        return type;
    }

    private String literal = "";

    public IFMLObject object = null;
    public IFMLObject getObject()
    {
        return object;
    }

    private String parameterName = "";

	public FormalElement(String formalElement, String parameter)
	{
		this.type = FormalElementTypeEnum.LITERAL;
		this.literal = formalElement;
        this.parameterName = parameter;
	}

	public FormalElement(IFMLObject object, String parameter)
	{
		this.type = FormalElementTypeEnum.OBJECT;
		this.object = object;
        this.parameterName = parameter;
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
}
