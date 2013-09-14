package ifml2.om;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement(name="word")
public class Word
{
    public Word()
    {
        super();
    }

    public Word(String ip)
    {
        this.ip = ip;
    }

    public static String getClassName()
    {
        return "Словарная запись";
    }

    @XmlEnum
    public enum GramCaseEnum
	{
		@XmlEnumValue(value = "ip")
        IP("ИП", "что"),
        @XmlEnumValue(value = "rp")
		RP("РП", "чего"),
        @XmlEnumValue(value = "dp")
		DP("ДП", "чему"),
        @XmlEnumValue(value = "vp")
		VP("ВП", "что"),
        @XmlEnumValue(value = "tp")
		TP("ТП", "чем"),
        @XmlEnumValue(value = "pp")
		PP("ПП", "чём");

        @XmlTransient
        private final String abbreviation;
        public String getAbbreviation()
        {
            return abbreviation;
        }

        @XmlTransient
        private final String questionWord;
        public String getQuestionWord()
        {
            return questionWord;
        }

        GramCaseEnum(String abbreviation, String questionWord)
        {
            this.abbreviation = abbreviation;
            this.questionWord = questionWord;
        }

        public static GramCaseEnum getValueByAbbr(String abbreviation)
        {
            for(GramCaseEnum caseEnum : values())
            {
                if(caseEnum.abbreviation.equalsIgnoreCase(abbreviation))
                {
                    return caseEnum;
                }
            }
            return null;
        }

        @Override
        public String toString()
        {
            return abbreviation;
        }
    }

    // todo перевести на HashMap<GramCase, string>
	@XmlElement(name="ip")
	@XmlID
	public String ip;
	@XmlElement(name="rp")
	public String rp;
	@XmlElement(name="dp")
	public String dp;
	@XmlElement(name="vp")
	public String vp;
	@XmlElement(name="tp")
	public String tp;
	@XmlElement(name="pp")
	public String pp;

    /**
     * IFML objects which is linked to the word
     * Are set in OMManager
     */
    @XmlTransient
    public final ArrayList<IFMLObject> linkerObjects = new ArrayList<IFMLObject>();
    public ArrayList<IFMLObject> getLinkerObjects()
    {
        return linkerObjects;
    }

    @Override
	public String toString()
	{
		return ip;
	}

	private String getFormOrIP(String form)
    {
        if(form == null || "".equals(form))
        {
            return "(" + ip + ")";
        }
        else
        {
            return form;
        }
    }

    public String getFormByGramCase(GramCaseEnum gramCase)
    {
		switch (gramCase)
		{
			case IP:
				return ip;

			case RP:
				return getFormOrIP(rp);

			case DP:
				return getFormOrIP(dp);

			case VP:
				return getFormOrIP(vp);

			case TP:
				return getFormOrIP(tp);

			case PP:
				return getFormOrIP(pp);

			default:
                throw new AssertionError("Лишний enum в Word.getFormByGramCase()!");
		}
	}
}
