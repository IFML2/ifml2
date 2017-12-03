package ifml2.tests;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class IFMLTest {
    @XmlElement(name = "testIteration")
    public final ArrayList<IFMLTestIteration> testIterations = new ArrayList<IFMLTestIteration>();
}
