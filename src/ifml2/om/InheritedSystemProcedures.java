package ifml2.om;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.NONE)
public class InheritedSystemProcedures implements Cloneable
{
    @XmlElement(name = "parseErrorHandler")
    @XmlIDREF()
    private Procedure parseErrorHandler;

    @Override
    public InheritedSystemProcedures clone() throws CloneNotSupportedException
    {
        return (InheritedSystemProcedures) super.clone();
    }

    public Procedure getParseErrorHandler()
    {
        return parseErrorHandler;
    }

    public void setParseErrorHandler(Procedure parseErrorHandler)
    {
        this.parseErrorHandler = parseErrorHandler;
    }
}
