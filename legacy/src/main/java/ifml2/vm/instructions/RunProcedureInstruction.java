package ifml2.vm.instructions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFML2Exception;
import ifml2.om.Procedure;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;
import ifml2.vm.Variable;
import ifml2.vm.values.Value;

import javax.xml.bind.annotation.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@IFML2Instruction(title = "Выполнить процедуру")
public class RunProcedureInstruction extends Instruction
{
    @XmlAttribute(name = "procedure")
    @XmlIDREF
    private Procedure procedure;

    @XmlAttribute(name = "returnToVar")
    private String returnToVar;

    @XmlElement(name = "parameter")
    private EventList<Procedure.FilledParameter> filledParameters = new BasicEventList<Procedure.FilledParameter>();

    @Override
    public String toString()
    {
        String returnMessage = returnToVar != null && !returnToVar.isEmpty() ? MessageFormat
                .format(" и вернуть результат в переменную \"{0}\"", returnToVar) : "";
        return MessageFormat.format("Вызвать процедуру \"{0}\"{1}", procedure, returnMessage);
    }

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        List<Variable> parameters = new ArrayList<Variable>(filledParameters.size());
        if(filledParameters != null)
        {
            for (Procedure.FilledParameter filledParameter : filledParameters)
            {
                String valueExpression = filledParameter.getValueExpression();
                String name = filledParameter.getName();
                Value value = null;
                try
                {
                    value = ExpressionCalculator.calculate(runningContext, valueExpression);
                }
                catch (IFML2Exception e)
                {
                    throw new IFML2VMException(e, "{0}\n  при вычислении значения ({1}) параметра {2}", e.getMessage(), valueExpression, name);
                }
                parameters.add(new Variable(name, value));
            }
        }

        Value returnValue = virtualMachine.callProcedureWithParameters(procedure, parameters);
        if (returnToVar != null && returnValue != null)
        {
            runningContext.writeVariable(returnToVar, returnValue);
        }
    }

    public Procedure getProcedure()
    {
        return procedure;
    }

    public String getReturnToVar()
    {
        return returnToVar;
    }

    public EventList<Procedure.FilledParameter> getParameters()
    {
        return filledParameters;
    }

    public Procedure.FilledParameter getParameterByName(String name)
    {
        for (Procedure.FilledParameter filledParameter : filledParameters)
        {
            String filledParameterName = filledParameter.getName();
            if (filledParameterName != null && filledParameterName.equalsIgnoreCase(name))
            {
                return filledParameter;
            }
        }

        return null;
    }

    public void setProcedure(Procedure procedure)
    {
        this.procedure = procedure;
    }

    public void setParameters(EventList<Procedure.FilledParameter> parameters)
    {
        this.filledParameters = parameters;
    }

    public void setReturnToVar(String returnToVar)
    {
        this.returnToVar = returnToVar;
    }
}
