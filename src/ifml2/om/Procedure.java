package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.vm.instructions.Instruction;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name="procedure")
public class Procedure
{
    @XmlEnum
    public enum SystemProcedureEnum
    {
        @XmlEnumValue(value = "showLocName")
        SHOW_LOC_NAME
    }
	private String name;

    @XmlAttribute(name="name")
	@XmlID
    void setName(String name) { this.name = name; }
    public String getName() { return name; }
    @XmlAttribute(name = "inheritsSystemProcedure")
    private final SystemProcedureEnum inheritsSystemProcedure = null;

    public SystemProcedureEnum getInheritsSystemProcedure() { return inheritsSystemProcedure; }
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private EventList<Parameter> parameters = new BasicEventList<Parameter>();

    @XmlElementWrapper(name = "procedureVariables")
    @XmlElement(name = "procedureVariable")
    private final EventList<ProcedureVariable> variables = new BasicEventList<ProcedureVariable>();
    public EventList<ProcedureVariable> getVariables() { return variables; }

    @XmlElement(name = "procedureBody")
    private final InstructionList procedureBody = new InstructionList();
    public InstructionList getProcedureBody() { return procedureBody; }

    public List<Instruction> getInstructions()
    {
        return procedureBody.getInstructions();
    }

    public Procedure(String name)
    {
        this();
        setName(name);
    }

    private Procedure()
    {
    }

    @Override
	public String toString()
	{
		return getName();
	}
}
