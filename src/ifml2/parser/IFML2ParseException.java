package ifml2.parser;

import ifml2.IFML2Exception;

public class IFML2ParseException extends IFML2Exception
{
    private static final long serialVersionUID = 7691101943043088649L;

    private int usedWords = 0;
    private int templateSize = 0;

    public IFML2ParseException(String message)
	{
		super(message);
	}

    public IFML2ParseException(String message, int usedWords)
    {
        super(message);
        this.setUsedWords(usedWords);
    }

    public IFML2ParseException(String message, int usedWords, int templateSize)
    {
        super(message);
        this.setUsedWords(usedWords);
        this.setTemplateSize(templateSize);
    }

    public boolean isMoreFull(IFML2ParseException exception, int currentTemplateSize)
    {
        return (getUsedWords() > exception.getUsedWords())
                || (getUsedWords() == exception.getUsedWords()) && (getTemplateSize() > currentTemplateSize);
    }

    public int getUsedWords()
    {
        return usedWords;
    }

    public void setUsedWords(int usedWords)
    {
        this.usedWords = usedWords;
    }

    public int getTemplateSize()
    {
        return templateSize;
    }

    public void setTemplateSize(int templateSize)
    {
        this.templateSize = templateSize;
    }
}
