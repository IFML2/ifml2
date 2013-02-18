package ifml2.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "procedureCall")
public class ProcedureCall
{
	private Procedure procedure;
	@XmlAttribute(name="procedure")
	@XmlIDREF
	public void setProcedure(Procedure procedure) { this.procedure = procedure; }
	public Procedure getProcedure() { return procedure; }

    @Override
	public String toString()
	{
		return procedure.toString();
	}
}
