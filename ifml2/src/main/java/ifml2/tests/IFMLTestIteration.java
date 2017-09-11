package ifml2.tests;

import javax.xml.bind.annotation.XmlElement;

class IFMLTestIteration {
    @XmlElement(name = "command")
    public String command;

    @XmlElement(name = "answer")
    public String answer;
}
