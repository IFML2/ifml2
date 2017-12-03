package ifml2.editor.gui.instructions;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.jetbrains.annotations.NotNull;

import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Location;
import ifml2.om.Story;
import ifml2.vm.instructions.GoToLocInstruction;
import ifml2.vm.instructions.Instruction;

public class GoToLocInstrEditor extends AbstractInstrEditor {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField locExprText;
    private JComboBox locationsCombo;
    private JRadioButton locRadio;
    private JRadioButton exprRadio;

    public GoToLocInstrEditor(Window owner, GoToLocInstruction instruction, Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(Instruction.getTitleFor(GoToLocInstruction.class), contentPane, buttonOK, buttonCancel);

        // set listeners
        ChangeListener radioChangeListener = e -> {
            locationsCombo.setEnabled(locRadio.isSelected());
            locExprText.setEnabled(exprRadio.isSelected());
        };
        locRadio.addChangeListener(radioChangeListener);
        exprRadio.addChangeListener(radioChangeListener);
        locationsCombo.addActionListener(e -> {
            Location location = (Location) locationsCombo.getSelectedItem();
            if (location != null) {
                locExprText.setText(location.getId());
            }
        });

        // set data

        String locationExpr = instruction.getLocationExpr();
        locationsCombo.setModel(new DefaultEventComboBoxModel<Location>(storyDataHelper.getLocations()));

        // detect if location expression is location id
        Location location = storyDataHelper.findLocationById(locationExpr);
        if (location != null || "".equals(locationExpr)) // location by id is found or expression is empty
        {
            locRadio.setSelected(true);
            locationsCombo.setSelectedItem(location);
        } else {
            exprRadio.setSelected(true);
        }
        locExprText.setText(locationExpr);
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return GoToLocInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        updateData(instruction);

        GoToLocInstruction goToLocInstruction = (GoToLocInstruction) instruction;
        if (locRadio.isSelected()) {
            Location location = (Location) locationsCombo.getSelectedItem();
            goToLocInstruction.setLocationExpr(location.getId());
        } else {
            goToLocInstruction.setLocationExpr(locExprText.getText());
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (locRadio.isSelected() && locationsCombo.getSelectedItem() == null) {
            throw new DataNotValidException("Не выбрана локация.", locationsCombo);
        }
        if (exprRadio.isSelected() && "".equals(locExprText.getText().trim())) {
            throw new DataNotValidException("Не введено выражение.", locExprText);
        }
    }
}
