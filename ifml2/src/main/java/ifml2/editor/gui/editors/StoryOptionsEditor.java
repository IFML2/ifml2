package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils.EventComboBoxModelWithNullElement;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.EditorUtils;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import ifml2.vm.instructions.SetVarInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static ifml2.om.Word.Gender.FEMININE;

public class StoryOptionsEditor extends AbstractEditor<StoryOptions> {
    private final static String STORY_OPTIONS_EDITOR_FORM_NAME = "Настройка истории";
    private final EventList<SetVarInstruction> varsClone;
    private final Story.DataHelper storyDataHelper;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox startLocCombo;
    private JComboBox startProcedureCombo;
    private JCheckBox showStartLocDescCheck;
    private JTextField nameText;
    private JTextField authorText;
    private JTextField versionText;
    private JTextArea descriptionTextArea;
    private ListEditForm<SetVarInstruction> globalVarListEditForm;
    private JCheckBox disHelpCheck;
    private JCheckBox disDebugCheck;

    public StoryOptionsEditor(Window owner, StoryOptions storyOptions, final Story.DataHelper storyDataHelper) {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        $$$setupUI$$$();
        initializeEditor(STORY_OPTIONS_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // clone data
        varsClone = GlazedLists.eventList(storyOptions.getVars());

        // -- init form --

        startProcedureCombo.setModel(new EventComboBoxModelWithNullElement<Procedure>(storyDataHelper.getProcedures(),
                storyOptions.getStartProcedureOption().getProcedure()));

        startLocCombo.setModel(new EventComboBoxModelWithNullElement<Location>(storyDataHelper.getLocations(),
                storyOptions.getStartLocationOption().getLocation()));

        showStartLocDescCheck.setSelected(storyOptions.getStartLocationOption().getShowStartLocDesc());

        // set vars
        globalVarListEditForm.bindData(varsClone);

        // set descr
        StoryOptions.StoryDescription storyDescription = storyOptions.getStoryDescription();
        nameText.setText(storyDescription.getName());
        authorText.setText(storyDescription.getAuthor());
        versionText.setText(storyDescription.getVersion());
        descriptionTextArea.setText(storyDescription.getDescription());

        // set disables
        StoryOptions.SystemCommandsDisableOption systemCommandsDisableOption = storyOptions.getSystemCommandsDisableOption();
        disHelpCheck.setSelected(systemCommandsDisableOption.isDisableHelp());
        disDebugCheck.setSelected(systemCommandsDisableOption.isDisableDebug());
    }

    @Override
    public void updateData(@NotNull StoryOptions data) {
        StoryOptions.StartLocationOption startLocationOption = data.getStartLocationOption();
        startLocationOption.setLocation((Location) startLocCombo.getSelectedItem());
        startLocationOption.setShowStartLocDesc(showStartLocDescCheck.isSelected());

        data.getStartProcedureOption().setProcedure((Procedure) startProcedureCombo.getSelectedItem());

        data.setVars(varsClone);

        StoryOptions.StoryDescription storyDescription = data.getStoryDescription();
        storyDescription.setName(nameText.getText());
        storyDescription.setAuthor(authorText.getText());
        storyDescription.setVersion(versionText.getText());
        storyDescription.setDescription(descriptionTextArea.getText());

        StoryOptions.SystemCommandsDisableOption systemCommandsDisableOption = data.getSystemCommandsDisableOption();
        systemCommandsDisableOption.setDisableHelp(disHelpCheck.isSelected());
        systemCommandsDisableOption.setDisableDebug(disDebugCheck.isSelected());
    }

    private void createUIComponents() {
        globalVarListEditForm = new ListEditForm<SetVarInstruction>(this, "глобальную переменную", "глобальной переменной", FEMININE,
                SetVarInstruction.class) {
            @Override
            protected SetVarInstruction createElement() throws Exception {
                SetVarInstruction setVarInstruction = new SetVarInstruction();
                return EditorUtils.showAssociatedEditor(StoryOptionsEditor.this, setVarInstruction, storyDataHelper) ? setVarInstruction
                        : null;

            }

            @Override
            protected boolean editElement(SetVarInstruction selectedElement) throws Exception {
                return EditorUtils.showAssociatedEditor(StoryOptionsEditor.this, selectedElement, storyDataHelper);
            }
        };
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
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
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPane.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Основные настройки", panel3);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder("Отключение системных команд"));
        disHelpCheck = new JCheckBox();
        disHelpCheck.setText("Отключить команду помощи");
        panel4.add(disHelpCheck, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        disDebugCheck = new JCheckBox();
        disDebugCheck.setText("Отключить режим отладки");
        panel4.add(disDebugCheck, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 2, new Insets(10, 10, 10, 10), -1, -1));
        panel3.add(panel5, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder("Старовые настройки"));
        final JSeparator separator1 = new JSeparator();
        panel5.add(separator1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 10), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Стартовая процедура");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 14), null, 0, false));
        startProcedureCombo = new JComboBox();
        panel5.add(startProcedureCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Стартовая локация");
        panel5.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startLocCombo = new JComboBox();
        panel5.add(startLocCombo, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showStartLocDescCheck = new JCheckBox();
        showStartLocDescCheck.setText("Показывать описание стартовой локации в начале игры");
        showStartLocDescCheck.setMnemonic('О');
        showStartLocDescCheck.setDisplayedMnemonicIndex(11);
        panel5.add(showStartLocDescCheck, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Глобальные переменные", panel6);
        panel6.add(globalVarListEditForm.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(4, 2, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Об истории и авторе", panel7);
        final JLabel label3 = new JLabel();
        label3.setText("Название");
        panel7.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameText = new JTextField();
        panel7.add(nameText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        authorText = new JTextField();
        panel7.add(authorText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Автор");
        panel7.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        versionText = new JTextField();
        panel7.add(versionText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Версия");
        panel7.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Описание");
        panel7.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel7.add(scrollPane1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        descriptionTextArea = new JTextArea();
        scrollPane1.setViewportView(descriptionTextArea);
        label1.setLabelFor(startProcedureCombo);
        label2.setLabelFor(startLocCombo);
        label3.setLabelFor(nameText);
        label4.setLabelFor(authorText);
        label5.setLabelFor(versionText);
        label6.setLabelFor(descriptionTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
