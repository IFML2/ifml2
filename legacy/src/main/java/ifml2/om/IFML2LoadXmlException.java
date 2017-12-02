package ifml2.om;

import javax.xml.bind.ValidationEvent;

import ifml2.IFML2Exception;

public class IFML2LoadXmlException extends IFML2Exception {
    private ValidationEvent[] events;

    public IFML2LoadXmlException(ValidationEvent[] events) {
        super("Error while loading Xml");
        this.events = events;
    }

    public ValidationEvent[] getEvents() {
        return events;
    }

    @Override
    public Throwable getCause() {
        if (events != null && events.length > 0) {
            return events[0].getLinkedException();
        } else {
            return super.getCause();
        }
    }
}
