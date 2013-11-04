package ifml2.om;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fuse")
public abstract class Fuse
{
    private String name = "";



    public String getName()
    {
        return name;
    }
}
