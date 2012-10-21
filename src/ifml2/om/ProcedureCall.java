package ifml2.om;

import ifml2.om.xml.xmladapters.ProcedureCallParametersAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "procedureCall")
public class ProcedureCall
{
	private Procedure procedure;
	@XmlAttribute(name="procedure")
	@XmlIDREF
	public void setProcedure(Procedure procedure) { this.procedure = procedure; }
	public Procedure getProcedure() { return procedure; }
	
	@XmlAttribute(name="parameters")
	@XmlJavaTypeAdapter(ProcedureCallParametersAdapter.class)
    private final List<Integer> parameters = new ArrayList<Integer>();

	@Override
	public String toString()
	{
		return procedure + "(" + parameters + ")";
	}
}
