package ifml2.editor.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

/**
 * Created by realsonic on 26.05.2016.
 */
public class DocChangeListener implements DocumentListener {
    private final Consumer<DocumentEvent> eventAction;

    public DocChangeListener(Consumer<DocumentEvent> eventAction) {
        this.eventAction = eventAction;
    }

    private void doEventAction(DocumentEvent e) {
        if (eventAction != null) {
            eventAction.accept(e);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        doEventAction(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        doEventAction(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        doEventAction(e);
    }
}
