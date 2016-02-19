package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFMLEntity;
import ifml2.vm.instructions.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

@XmlAccessorType(XmlAccessType.NONE)
public class InstructionList extends IFMLEntity {
    @XmlElements({
            @XmlElement(name = "goToLoc", type = GoToLocInstruction.class),
            @XmlElement(name = "showMessage", type = ShowMessageInstr.class),
            @XmlElement(name = "if", type = IfInstruction.class),
            @XmlElement(name = "loop", type = LoopInstruction.class),
            @XmlElement(name = "var", type = SetVarInstruction.class),
            @XmlElement(name = "return", type = ReturnInstruction.class),
            @XmlElement(name = "setProperty", type = SetPropertyInstruction.class),
            @XmlElement(name = "moveItem", type = MoveItemInstruction.class),
            @XmlElement(name = "rollDice", type = RollDiceInstruction.class),
            @XmlElement(name = "runProcedure", type = RunProcedureInstruction.class),
            @XmlElement(name = "showPicture", type = ShowPictureInstruction.class)
    })
    private EventList<Instruction> instructions = new BasicEventList<>(); // InstructionList controls its instructions

    public EventList<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public InstructionList clone() throws CloneNotSupportedException {
        InstructionList clone = (InstructionList) super.clone(); // flat clone

        // deep clone
        clone.instructions = deepCloneEventList(instructions, Instruction.class);

        return clone;
    }

    public void replaceInstructions(InstructionList instructionList) { //todo change to copyTo from whole clone
        instructions.clear();
        instructions.addAll(instructionList.getInstructions());
    }
}
