package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.om.InstructionList;
import ifml2.vm.RunningContext;
import ifml2.vm.Variable;
import ifml2.vm.values.ObjectValue;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "loop")
public class LoopInstruction extends Instruction
{
    @XmlAttribute(name = "collection")
    private
    String collectionExpression;

    @XmlAttribute(name = "element")
    private
    String elementName;

    @XmlAttribute(name = "condition")
    private
    String conditionExpression;

    @XmlElement(name = "empty")
    private final
    InstructionList emptyInstructions = null;

    @XmlElement(name = "alone")
    private final
    InstructionList aloneInstructions = null;

    @XmlElement(name = "first")
    private final
    InstructionList firstInstructions = null;

    @XmlElement(name = "next")
    private final
    InstructionList nextInstructions = null;

    @XmlElement(name = "last")
    private final
    InstructionList lastInstructions = null;

    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        // get the collection
        List<IFMLObject> collection = (List<IFMLObject>) getCollectionFromExpression(collectionExpression, runningContext, getTitle(), "Коллекция");
        //todo solve unsafe cast problem...

        List<IFMLObject> filteredCollection = new ArrayList<IFMLObject>();

        // iterate the collection and filter it
        if (conditionExpression != null && !"".equals(conditionExpression))
        {
            for (IFMLObject element : collection)
            {
                runningContext.setVariable(Variable.VariableScope.LOCAL, elementName, new ObjectValue(element));

                Boolean condition = getBooleanFromExpression(conditionExpression, runningContext, getTitle(), "Условие");

                if (condition)
                {
                    filteredCollection.add(element);
                }
            }
        }
        else
        {
            // there is no condition - so get all the collection
            filteredCollection = collection;
        }

        runningContext.deleteVariable(Variable.VariableScope.LOCAL, elementName);

        int elementsQuantity = filteredCollection.size();

        // run clauses

        switch (elementsQuantity)
        {
            case 0:
                if (emptyInstructions != null)
                {
                    virtualMachine.runInstructionList(emptyInstructions, runningContext, true, true);
                }
                break;

            case 1:
                runningContext.setVariable(Variable.VariableScope.LOCAL, elementName, new ObjectValue(filteredCollection.get(0)));
                if (aloneInstructions != null)
                {
                    virtualMachine.runInstructionList(aloneInstructions, runningContext, true, true);
                    break;
                }
                if (firstInstructions != null)
                {
                    virtualMachine.runInstructionList(firstInstructions, runningContext, true, true);
                    break;
                }
                if (nextInstructions != null)
                {
                    virtualMachine.runInstructionList(nextInstructions, runningContext, true, true);
                    break;
                }
                if (lastInstructions != null)
                {
                    virtualMachine.runInstructionList(lastInstructions, runningContext, true, true);
                    break;
                }
                break;

            default:
                for (int index = 0; index <= elementsQuantity - 1; index++)
                {
                    runningContext.setVariable(Variable.VariableScope.LOCAL, elementName, new ObjectValue(filteredCollection.get(index)));

                    if (index == 0) // first element
                    {
                        if (firstInstructions != null)
                        {
                            virtualMachine.runInstructionList(firstInstructions, runningContext, true, true);
                        }
                        else if (nextInstructions != null)
                        {
                            virtualMachine.runInstructionList(nextInstructions, runningContext, true, true);
                        }
                    }
                    else if (index == elementsQuantity - 1) // last element
                    {
                        if (lastInstructions != null)
                        {
                            virtualMachine.runInstructionList(lastInstructions, runningContext, true, true);
                        }
                        else if (nextInstructions != null)
                        {
                            virtualMachine.runInstructionList(nextInstructions, runningContext, true, true);
                        }
                    }
                    else // other elements
                    {
                        if (nextInstructions != null)
                        {
                            virtualMachine.runInstructionList(nextInstructions, runningContext, true, true);
                        }
                    }
                }
        }
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("Цикл: для каждого \"{0}\" из \"{1}\" с учётом \"{2}\"", elementName, collectionExpression, conditionExpression);
    }

    public static String getTitle()
    {
        return "Цикл";
    }
}
