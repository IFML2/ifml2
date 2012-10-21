package ifml2.parser;

import java.util.ArrayList;

public class IFML2ParsePhraseTooLong extends IFML2ParseException
{
    private static final long serialVersionUID = 6741264251197068246L;

    public ArrayList<Parser.FittedFormalElement> fittedFormalElements = new ArrayList<Parser.FittedFormalElement>();

    public IFML2ParsePhraseTooLong(String message, ArrayList<Parser.FittedFormalElement> fittedFormalElement, int usedWordsQty)
    {
        super(message);
        this.fittedFormalElements = fittedFormalElement;
        this.usedWords = usedWordsQty;
    }
}
