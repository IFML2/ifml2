package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "procedureCall")
public class ProcedureCall
{
    private Procedure procedure;

    @XmlAttribute(name = "procedure")
    @XmlIDREF
    public Procedure getProcedure()
    {
        return procedure;
    }

    public void setProcedure(Procedure procedure)
    {
        this.procedure = procedure;
    }

    @Override
    public String toString()
    {
        return procedure != null ? procedure.toString() : "";
    }
}
