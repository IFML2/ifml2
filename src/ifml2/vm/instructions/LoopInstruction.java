package ifml2.vm.instructions;

import ifml2.IFML2Exception;
import ifml2.om.IFMLObject;
import ifml2.om.InstructionList;
import ifml2.vm.RunningContext;
import ifml2.vm.values.ObjectValue;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "loop")
@IFML2Instruction(title = "Цикл")
public class LoopInstruction extends Instruction
{
    @XmlElement(name = "empty")
    private final InstructionList emptyInstructions = null;

    @XmlElement(name = "alone")
    private final InstructionList aloneInstructions = null;

    @XmlElement(name = "first")
    private final InstructionList firstInstructions = null;

    @XmlElement(name = "next")
    private final InstructionList nextInstructions = null;

    @XmlElement(name = "last")
    private final InstructionList lastInstructions = null;

    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = "collection")
    private String collectionExpression;

    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = "element")
    private String elementName;

    @SuppressWarnings("UnusedDeclaration")
    @XmlAttribute(name = "condition")
    private String conditionExpression;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(RunningContext runningContext) throws IFML2Exception
    {
        // get the collection
        List<IFMLObject> collection = convertToClassedList(
                getCollectionFromExpression(collectionExpression, runningContext, getTitle(), "Коллекция"), IFMLObject.class);

        List<IFMLObject> filteredCollection = new ArrayList<IFMLObject>();

        RunningContext nestedContext = RunningContext.CreateNestedContext(runningContext);

        // iterate the collection and filter it
        if (conditionExpression != null && !"".equals(conditionExpression))
        {
            for (IFMLObject element : collection)
            {
                nestedContext.writeVariable(elementName, new ObjectValue(element));

                Boolean condition = getBooleanFromExpression(conditionExpression, nestedContext, getTitle(), "Условие");

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

        int elementsQuantity = filteredCollection.size();

        // run clauses

        switch (elementsQuantity)
        {
            case 0:
                if (emptyInstructions != null)
                {
                    virtualMachine.runInstructionList(emptyInstructions, nestedContext);
                }
                break;

            case 1:
                nestedContext.writeVariable(elementName, new ObjectValue(filteredCollection.get(0)));
                if (aloneInstructions != null)
                {
                    virtualMachine.runInstructionList(aloneInstructions, nestedContext);
                    break;
                }
                if (firstInstructions != null)
                {
                    virtualMachine.runInstructionList(firstInstructions, nestedContext);
                    break;
                }
                if (nextInstructions != null)
                {
                    virtualMachine.runInstructionList(nextInstructions, nestedContext);
                    break;
                }
                if (lastInstructions != null)
                {
                    virtualMachine.runInstructionList(lastInstructions, nestedContext);
                    break;
                }
                break;

            default:
                for (int index = 0; index <= elementsQuantity - 1; index++)
                {
                    nestedContext.writeVariable(elementName, new ObjectValue(filteredCollection.get(index)));

                    if (index == 0) // first element
                    {
                        if (firstInstructions != null)
                        {
                            virtualMachine.runInstructionList(firstInstructions, nestedContext);
                        }
                        else if (nextInstructions != null)
                        {
                            virtualMachine.runInstructionList(nextInstructions, nestedContext);
                        }
                    }
                    else if (index == elementsQuantity - 1) // last element
                    {
                        if (lastInstructions != null)
                        {
                            virtualMachine.runInstructionList(lastInstructions, nestedContext);
                        }
                        else if (nextInstructions != null)
                        {
                            virtualMachine.runInstructionList(nextInstructions, nestedContext);
                        }
                    }
                    else // other elements
                    {
                        if (nextInstructions != null)
                        {
                            virtualMachine.runInstructionList(nextInstructions, nestedContext);
                        }
                    }
                }
        }
    }

    @Override
    public String toString()
    {
        return MessageFormat
                .format("Цикл: для каждого \"{0}\" из \"{1}\" с условием \"{2}\"", elementName, collectionExpression, conditionExpression);
    }
}
