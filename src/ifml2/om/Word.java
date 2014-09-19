package ifml2.om;

import ifml2.IFMLEntity;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement(name = "word")
@XmlAccessorType(XmlAccessType.NONE)
public class Word extends IFMLEntity
{
    /**
     * IFML objects which is linked to the word
     * Are set in OMManager
     */
    @XmlTransient
    private final ArrayList<IFMLObject> linkerObjects = new ArrayList<IFMLObject>();

    // todo перевести на HashMap<GramCase, string>
    @XmlElement(name = "ip")
    @XmlID
    public String ip;

    @XmlElement(name = "rp")
    public String rp;

    @XmlElement(name = "dp")
    public String dp;

    @XmlElement(name = "vp")
    public String vp;

    @XmlElement(name = "tp")
    public String tp;

    @XmlElement(name = "pp")
    public String pp;

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

    public void addLinkerObject(IFMLObject ifmlObject)
    {
        if (!linkerObjects.contains(ifmlObject))
        {
            linkerObjects.add(ifmlObject);
        }
    }

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
        if (form == null || "".equals(form))
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

    @XmlEnum
    public enum GramCaseEnum
    {
        @XmlEnumValue(value = "ip")
        IP("ИП", "кто (что)"),
        @XmlEnumValue(value = "rp")
        RP("РП", "кого (чего)"),
        @XmlEnumValue(value = "dp")
        DP("ДП", "кому (чему)"),
        @XmlEnumValue(value = "vp")
        VP("ВП", "кого (что)"),
        @XmlEnumValue(value = "tp")
        TP("ТП", "кем (чем)"),
        @XmlEnumValue(value = "pp")
        PP("ПП", "ком (чём)");

        @XmlTransient
        private final String abbreviation;
        @XmlTransient
        private final String questionWord;

        GramCaseEnum(String abbreviation, String questionWord)
        {
            this.abbreviation = abbreviation;
            this.questionWord = questionWord;
        }

        public static GramCaseEnum getValueByAbbr(String abbreviation)
        {
            for (GramCaseEnum caseEnum : values())
            {
                if (caseEnum.abbreviation.equalsIgnoreCase(abbreviation))
                {
                    return caseEnum;
                }
            }
            return null;
        }

        public String getAbbreviation()
        {
            return abbreviation;
        }

        public String getQuestionWord()
        {
            return questionWord;
        }

        @Override
        public String toString()
        {
            return abbreviation;
        }
    }

    public static enum GenderEnum
    {
        MASCULINE,
        FEMININE,
        NEUTER
    }
}
