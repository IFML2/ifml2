package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "procedureCall")
public class ProcedureCall
{
	@XmlAttribute(name="procedure")
	@XmlIDREF
    private Procedure procedure;
    public Procedure getProcedure() { return procedure; }

    @Override
	public String toString()
	{
		return procedure != null ? procedure.toString() : "";
	}
}
