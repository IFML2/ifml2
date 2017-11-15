package ifml2.parser;

import java.util.ArrayList;
import java.util.List;

import ifml2.om.Action;

public class ParseResult {

    public Action action = null;
    public List<FormalElement> formalElements = null;

    public ParseResult(Action action, List<FormalElement> formalElements) {
        this.action = action;
        this.formalElements = formalElements;
    }

    public Action getAction() {
        return action;
    }

    public List<FormalElement> getFormalElements() {
        return formalElements;
    }

    @Override
    public String toString() {
        return "Действие = [" + action +
                "], формальные элементы = [" + formalElements + ']';
    }

}
