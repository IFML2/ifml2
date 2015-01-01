package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFMLEntity;
import ifml2.vm.instructions.Instruction;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "procedure")
@XmlAccessorType(XmlAccessType.NONE)
public class Procedure extends IFMLEntity implements Cloneable
{
    @XmlAttribute(name = "inheritsSystemProcedure")
    private SystemProcedureEnum inheritsSystemProcedure = null;
    @XmlElementWrapper(name = "procedureVariables")
    @XmlElement(name = "procedureVariable")
    private EventList<ProcedureVariable> variables = new BasicEventList<ProcedureVariable>();
    @XmlElement(name = "procedureBody")
    private InstructionList procedureBody = new InstructionList();
    @XmlAttribute(name = "name")
    @XmlID
    private String name;
    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private EventList<Parameter> parameters = new BasicEventList<Parameter>();

    public Procedure(String name)
    {
        this.name = name;
    }

    public Procedure()
    {
    }

    public Parameter getParameterByName(String parameterName)
    {
        for (Parameter parameter : parameters)
        {
            if (parameter.getName().equalsIgnoreCase(parameterName))
            {
                return parameter;
            }
        }

        return null;
    }

    public String getName()
    {
        return name;
    }

    public SystemProcedureEnum getInheritsSystemProcedure()
    {
        return inheritsSystemProcedure;
    }

    public EventList<Parameter> getParameters()
    {
        return parameters;
    }

    public EventList<ProcedureVariable> getVariables()
    {
        return variables;
    }

    public InstructionList getProcedureBody()
    {
        return procedureBody;
    }

    public EventList<Instruction> getInstructions()
    {
        return procedureBody.getInstructions();
    }

    @Override
    public Procedure clone() throws CloneNotSupportedException
    {
        Procedure clone = (Procedure) super.clone(); // clone flat fields

        // clone deeper
        clone.variables = deepCloneEventList(variables, ProcedureVariable.class);
        clone.procedureBody = procedureBody.clone();
        clone.parameters = deepCloneEventList(parameters, Parameter.class);

        return clone;
    }

    public void copyTo(Procedure procedure) throws CloneNotSupportedException
    {
        procedure.name = name;
        procedure.inheritsSystemProcedure = inheritsSystemProcedure;
        procedure.variables = deepCloneEventList(variables, ProcedureVariable.class);
        procedure.procedureBody = procedureBody.clone();
        procedure.parameters = deepCloneEventList(parameters, Parameter.class);
    }

    @Override
    public String toString()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlEnum
    public enum SystemProcedureEnum
    {
        @XmlEnumValue(value = "showLocName")
        SHOW_LOC_NAME
    }
}
