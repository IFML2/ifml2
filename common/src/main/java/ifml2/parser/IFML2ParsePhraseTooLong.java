package ifml2.parser;

import java.util.ArrayList;

public class IFML2ParsePhraseTooLong extends IFML2ParseException {
    private static final long serialVersionUID = 6741264251197068246L;

    private ArrayList<ParserImpl.FittedFormalElement> fittedFormalElements = new ArrayList<ParserImpl.FittedFormalElement>();
    private ArrayList<String> phraseRest = new ArrayList<String>();

    public IFML2ParsePhraseTooLong(ArrayList<ParserImpl.FittedFormalElement> fittedFormalElement, ArrayList<String> phraseRest, int usedWordsQty) {
        super("");
        this.setFittedFormalElements(fittedFormalElement);
        this.setPhraseRest(phraseRest);
        this.setUsedWords(usedWordsQty);
    }

    public ArrayList<ParserImpl.FittedFormalElement> getFittedFormalElements() {
        return fittedFormalElements;
    }

    public void setFittedFormalElements(ArrayList<ParserImpl.FittedFormalElement> fittedFormalElements) {
        this.fittedFormalElements = fittedFormalElements;
    }

    public ArrayList<String> getPhraseRest() {
        return phraseRest;
    }

    public void setPhraseRest(ArrayList<String> phraseRest) {
        this.phraseRest = phraseRest;
    }
}
