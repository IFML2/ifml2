package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.vm.instructions.Instruction;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="procedure")
public class Procedure
{
    public Parameter getParameterByName(String parameterName)
    {
        for(Parameter parameter : parameters)
        {
            if(parameter.getName().equalsIgnoreCase(parameterName))
            {
                return parameter;
            }
        }

        return null;
    }

    @XmlEnum
    public enum SystemProcedureEnum
    {
        @XmlEnumValue(value = "showLocName")
        SHOW_LOC_NAME
    }

    @XmlAttribute(name="name")
    @XmlID
    private String name;
    public String getName() { return name; }

    @XmlAttribute(name = "inheritsSystemProcedure")
    private final SystemProcedureEnum inheritsSystemProcedure = null;
    public SystemProcedureEnum getInheritsSystemProcedure() { return inheritsSystemProcedure; }

    private EventList<Parameter> parameters = new BasicEventList<Parameter>();
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public EventList<Parameter> getParameters() { return parameters; }

    @XmlElementWrapper(name = "procedureVariables")
    @XmlElement(name = "procedureVariable")
    private final EventList<ProcedureVariable> variables = new BasicEventList<ProcedureVariable>();
    public EventList<ProcedureVariable> getVariables() { return variables; }

    @XmlElement(name = "procedureBody")
    private final InstructionList procedureBody = new InstructionList();
    public InstructionList getProcedureBody() { return procedureBody; }

    public EventList<Instruction> getInstructions()
    {
        return procedureBody.getInstructions();
    }

    public Procedure(String name)
    {
        this.name = name;
    }

    public Procedure()
    {
    }

    @Override
	public String toString()
	{
		return getName();
	}
}
