package ifml2.om;

import ifml2.vm.instructions.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.ArrayList;
import java.util.List;

public class InstructionList implements Cloneable
{
    @XmlElements({
            @XmlElement(name = "showLocName", type = ShowLocNameInstruction.class),
            @XmlElement(name = "goToLoc", type = GoToLocInstruction.class),
            @XmlElement(name = "showItemDesc", type = ShowItemDescInstruction.class),
            @XmlElement(name = "showInventory", type = ShowInventoryInstruction.class),
            @XmlElement(name = "getItem", type = GetItemInstruction.class),
            @XmlElement(name = "showMessage", type = ShowMessageInstr.class),
            @XmlElement(name = "dropItem", type = DropItemInstruction.class),
            @XmlElement(name = "if", type = IfInstruction.class),
            @XmlElement(name = "loop", type = LoopInstruction.class),
            @XmlElement(name = "var", type = SetVarInstruction.class),
            @XmlElement(name = "return", type = ReturnInstruction.class),
            @XmlElement(name = "setProperty", type = SetPropertyInstruction.class),
            @XmlElement(name = "moveItem", type = MoveItemInstruction.class)
    })
    private List<Instruction> instructions = new ArrayList<Instruction>(); // InstructionList controls its instructions
    public List<Instruction> getInstructions() { return instructions; }

    @Override
    public InstructionList clone() throws CloneNotSupportedException
    {
        InstructionList clone = (InstructionList) super.clone();
        clone.instructions =  new ArrayList<Instruction>(instructions.size());
        for(Instruction instruction : instructions)
        {
            clone.instructions.add(instruction.clone());
        }
        return clone;
    }
}
