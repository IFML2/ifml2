package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Item;
import ifml2.om.Story;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.MoveItemInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MoveItemInstrEditor extends AbstractInstrEditor {
    private static final String MOVE_ITEM_EDITOR_TITLE = Instruction.getTitleFor(MoveItemInstruction.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField itemExprText;
    private JTextField toCollectionExprText;
    private JRadioButton itemRadio;
    private JRadioButton itemExprRadio;
    private JComboBox itemCombo;

    public MoveItemInstrEditor(Window owner, MoveItemInstruction instruction, Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(MOVE_ITEM_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // set listeners
        ChangeListener radioChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                itemCombo.setEnabled(itemRadio.isSelected());
                itemExprText.setEnabled(itemExprRadio.isSelected());
            }
        };
        itemRadio.addChangeListener(radioChangeListener);
        itemExprRadio.addChangeListener(radioChangeListener);
        itemCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Item item = (Item) itemCombo.getSelectedItem();
                if (item != null) {
                    itemExprText.setText(item.getId());
                }
            }
        });

        // load data
        String itemExpr = instruction.getItemExpr();
        itemCombo.setModel(new DefaultEventComboBoxModel<Item>(storyDataHelper.getItems()));
        itemExprText.setText(instruction.getItemExpr());
        // detect if item expression is item id
        Item item = storyDataHelper.findItemById(itemExpr);
        if (item != null || "".equals(itemExpr)) // item by id is found or expression is empty (for new instruction)
        {
            itemRadio.setSelected(true);
            itemCombo.setSelectedItem(item);
        } else {
            itemExprRadio.setSelected(true);
        }
        itemExprText.setText(itemExpr);

        toCollectionExprText.setText(instruction.getToCollectionExpr());
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return MoveItemInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        updateData(instruction);

        MoveItemInstruction moveItemInstruction = (MoveItemInstruction) instruction;

        // set item expr
        if (itemRadio.isSelected()) {
            Item item = (Item) itemCombo.getSelectedItem();
            moveItemInstruction.setItemExpr(item.getId());
        } else {
            moveItemInstruction.setItemExpr(itemExprText.getText());
        }

        // set loc expr
        moveItemInstruction.setItemExpr(itemExprText.getText());
        moveItemInstruction.setToCollectionExpr(toCollectionExprText.getText());
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (itemRadio.isSelected() && itemCombo.getSelectedItem() == null) {
            throw new DataNotValidException("Не выбран предмет.", itemCombo);
        }
        if (itemExprRadio.isSelected() && "".equals(itemExprText.getText().trim())) {
            throw new DataNotValidException("Не введено выражение для предмета.", itemExprText);
        }
        if ("".equals(toCollectionExprText.getText().trim())) {
            throw new DataNotValidException("Не введено выражение для коллекции.", toCollectionExprText);
        }
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonCancel.setMnemonic('C');
        buttonCancel.setDisplayedMnemonicIndex(0);
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Что переместить (предмет):");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Куда переместить (коллекция):");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        toCollectionExprText = new JTextField();
        toCollectionExprText.setEditable(true);
        panel3.add(toCollectionExprText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(384, 22), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(384, 69), null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        itemRadio = new JRadioButton();
        itemRadio.setText("предмет:");
        itemRadio.setMnemonic('П');
        itemRadio.setDisplayedMnemonicIndex(0);
        panel4.add(itemRadio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        itemExprRadio = new JRadioButton();
        itemExprRadio.setText("выражение:");
        itemExprRadio.setMnemonic('В');
        itemExprRadio.setDisplayedMnemonicIndex(0);
        panel4.add(itemExprRadio, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        itemExprText = new JTextField();
        itemExprText.setEditable(true);
        panel4.add(itemExprText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, -1), null, 0, false));
        itemCombo = new JComboBox();
        panel4.add(itemCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label1.setLabelFor(itemExprText);
        label2.setLabelFor(toCollectionExprText);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(itemRadio);
        buttonGroup.add(itemExprRadio);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
