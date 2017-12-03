package ifml2.parser;

import java.util.ArrayList;
import java.util.List;

public class IFML2ParsePhraseTooLong extends IFML2ParseException {
    private static final long serialVersionUID = 6741264251197068246L;

    private List<Parser.FittedFormalElement> fittedFormalElements = new ArrayList<>();
    private List<String> phraseRest = new ArrayList<>();

    public IFML2ParsePhraseTooLong(ArrayList<Parser.FittedFormalElement> fittedFormalElement, List<String> phraseRest,
            int usedWordsQty) {
        super("");
        this.setFittedFormalElements(fittedFormalElement);
        this.setPhraseRest(phraseRest);
        this.setUsedWords(usedWordsQty);
    }

    public List<Parser.FittedFormalElement> getFittedFormalElements() {
        return fittedFormalElements;
    }

    public void setFittedFormalElements(List<Parser.FittedFormalElement> fittedFormalElements) {
        this.fittedFormalElements = fittedFormalElements;
    }

    public List<String> getPhraseRest() {
        return phraseRest;
    }

    public void setPhraseRest(List<String> phraseRest) {
        this.phraseRest = phraseRest;
    }
}
