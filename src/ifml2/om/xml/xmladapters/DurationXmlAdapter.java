package ifml2.om.xml.xmladapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Duration;

public class DurationXmlAdapter extends XmlAdapter<String, Duration> {
    @Override
    public Duration unmarshal(String stringValue) {
        return stringValue != null ? Duration.parse(stringValue) : null;
    }

    @Override
    public String marshal(Duration value) {
        return value != null ? value.toString() : null;
    }
}
