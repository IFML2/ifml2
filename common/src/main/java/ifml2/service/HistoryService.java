package ifml2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class HistoryService implements Ifml2Service {

    private final List<String> history = new ArrayList<>();
    private ListIterator<String> historyIterator = history.listIterator();

    @Override
    public String getName() {
        return "HistoryService";
    }

    @Override
    public void start() {
        // nothing to do
    }

    @Override
    public void stop() {
        // nothing to do
    }

    public String prev() {
        return historyIterator.hasPrevious() ? historyIterator.previous() : "";
    }

    public String next() {
        return historyIterator.hasNext() ? historyIterator.next() : "";
    }

    public void update(String command) {
        history.add(command);
        historyIterator = history.listIterator(history.size());
    }

}
