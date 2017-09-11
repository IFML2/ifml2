package ifml2.parser;

import ifml2.IFML2Exception;

public class ParseException extends IFML2Exception {
    private static final long serialVersionUID = 7691101943043088649L;

    public int usedWords = 0;
    public int templateSize = 0;

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, int usedWords) {
        super(message);
        this.usedWords = usedWords;
    }

    public ParseException(String message, int usedWords, int templateSize) {
        super(message);
        this.usedWords = usedWords;
        this.templateSize = templateSize;
    }

    public boolean isMoreFull(ParseException exception, int currentTemplateSize) {
        return (usedWords > exception.usedWords)
                || (usedWords == exception.usedWords) && (templateSize > currentTemplateSize);
    }
}
