package ifml2.tests;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "testPlan")
public class IFMLTestPlan {
    @XmlElement(name = "test")
    public final IFMLTest test = new IFMLTest();
    @XmlAttribute(name = "name")
    public String name;
    @XmlElement(name = "storyLink")
    public String storyLink;
    @XmlTransient
    public String testFile;

    @Override
    public String toString() {
        return name + " -> " + storyLink;
    }

    public int getSize() {
        return test.testIterations.size();
    }

    public String getCommand(int index) {
        return test.testIterations.get(index).command;
    }

    public String getCommandWithAnswer(int index) {
        IFMLTestIteration testIteration = test.testIterations.get(index);
        return testIteration.command + " > " + testIteration.answer;
    }
}
