package ifml2.tests;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

public class IFMLTest {
    @XmlElement(name = "testIteration")
    public final ArrayList<IFMLTestIteration> testIterations = new ArrayList<IFMLTestIteration>();
}
