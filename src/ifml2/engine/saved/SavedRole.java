package ifml2.engine.saved;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;

public class SavedRole
{
    @XmlElementWrapper(name = "props")
    @XmlElement(name = "prop")
    private ArrayList<SavedProperty> properties = new ArrayList<SavedProperty>();

    @SuppressWarnings("UnusedDeclaration")
    public SavedRole()
    {
        // public no-args constructor for JAXB
    }

    @XmlAttribute(name = "name")
    private String name;

    public SavedRole(String name)
    {
        this.name = name;
    }

    public SavedProperty addProperty(String propertyName)
    {
        SavedProperty savedProperty = new SavedProperty(propertyName);
        getProperties().add(savedProperty);
        return savedProperty;
    }

    public String getName()
    {
        return name;
    }

    public ArrayList<SavedProperty> getProperties()
    {
        return properties;
    }
}
