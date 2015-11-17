package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFML2Exception;
import ifml2.IFMLEntity;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.RunningContext;
import ifml2.vm.Variable;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

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

    public void setName(String name)
    {
        this.name = name;
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

    public Variable searchProcedureVariable(String name, RunningContext runningContext) throws IFML2Exception
    {
        if(name == null)
        {
            return null;
        }

        String loweredName = name.toLowerCase();

        for (ProcedureVariable procedureVariable : variables)
        {
            if (loweredName.equalsIgnoreCase(procedureVariable.getName()))
            {
                if(procedureVariable.getValue() == null)
                {
                    // init value
                    Value value = ExpressionCalculator.calculate(runningContext, procedureVariable.getInitialValue());
                    procedureVariable.setValue(value);
                }

                return new ProcedureVariableProxy(procedureVariable);
            }
        }

        return null;
    }

    @XmlEnum
    public enum SystemProcedureEnum
    {
        @XmlEnumValue(value = "showLocation")
        SHOW_LOCATION("Описать локацию"),
        @XmlEnumValue(value = "parseErrorHandler")
        PARSE_ERROR_HANDLER("Обработка ошибок парсинга");

        private String name;

        SystemProcedureEnum(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class FilledParameter
    {
        @XmlAttribute(name = "name")
        private String name;
        @XmlAttribute(name = "value")
        private String valueExpression;

        @SuppressWarnings("UnusedDeclaration")
        public FilledParameter()
        {
            // used for JAXB
        }

        public FilledParameter(String name, String valueExpression)
        {
            this.name = name;
            this.valueExpression = valueExpression;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public String getName()
        {
            return name;
        }

        public String getValueExpression()
        {
            return valueExpression;
        }

        public void setValueExpression(String valueExpression)
        {
            this.valueExpression = valueExpression;
        }
    }

    private class ProcedureVariableProxy extends Variable
    {
        private ProcedureVariable procedureVariable;

        public ProcedureVariableProxy(@NotNull ProcedureVariable procedureVariable)
        {
            super(procedureVariable.getName(), procedureVariable.getValue());
            this.procedureVariable = procedureVariable;
        }

        @Override
        public Value getValue()
        {
            return procedureVariable.getValue();
        }

        @Override
        public void setValue(Value value)
        {
            procedureVariable.setValue(value);
        }

        @Override
        public String getName()
        {
            return procedureVariable.getName();
        }

        @Override
        public void setName(String name)
        {
            throw new RuntimeException("Внутренняя ошибка: Запрещено менять имена переменных");
        }
    }
}
