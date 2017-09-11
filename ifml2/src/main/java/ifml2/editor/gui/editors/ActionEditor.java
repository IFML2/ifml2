package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Action;
import ifml2.om.Procedure;
import ifml2.om.Restriction;
import ifml2.om.Story;
import ifml2.om.Template;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

public class ActionEditor extends AbstractEditor<Action> {
    private final EventList<Template> templatesClone;
    private final EventList<Restriction> restrictionsClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameText;
    private JTextField descriptionText;
    private JComboBox procedureCallCombo;
    private JList templatesList;
    private JButton addTemplateButton;
    private JButton editTemplateButton;
    private JButton delTemplateButton;
    private JList restrictionsList;
    private JButton upRestrictionButton;
    private JButton downRestrictionButton;
    private JButton addRestrictionButton;
    private JButton editRestrictionButton;
    private JButton delRestrictionButton;
    private Story.DataHelper storyDataHelper;

    public ActionEditor(Window owner, @NotNull Action action, Story.DataHelper storyDataHelper) {
        super(owner);
        this.storyDataHelper = storyDataHelper;
        initializeEditor("Действие", contentPane, buttonOK, buttonCancel);

        // init actions and listeners
        addTemplateButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Template template = new Template();
                if (editTemplate(template)) {
                    templatesClone.add(template);
                    templatesList.setSelectedValue(template, true);
                }
            }
        });
        editTemplateButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                templatesList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!templatesList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Template template = (Template) templatesList.getSelectedValue();
                if (template != null) {
                    editTemplate(template);
                }
            }
        });
        delTemplateButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                templatesList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!templatesList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(ActionEditor.this, "Вы действительно хотите удалить этот шаблон?",
                        "Удаление шаблона", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    Template selectedAction = (Template) templatesList.getSelectedValue();
                    templatesClone.remove(selectedAction);
                }
            }
        });
        addRestrictionButton.setAction(new AbstractAction("Добавить...", GUIUtils.NEW_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Restriction restriction = new Restriction();
                if (editRestriction(restriction)) {
                    restrictionsClone.add(restriction);
                    restrictionsList.setSelectedValue(restriction, true);
                }
            }
        });
        editRestrictionButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!restrictionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Restriction restriction = (Restriction) restrictionsList.getSelectedValue();
                if (restriction != null) {
                    editRestriction(restriction);
                }
            }
        });
        delRestrictionButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!restrictionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Restriction restriction = (Restriction) restrictionsList.getSelectedValue();
                if (restriction != null &&
                        GUIUtils.showDeleteConfirmDialog(ActionEditor.this, "ограничение", "ограничения", Word.Gender.NEUTER)) {
                    restrictionsClone.remove(restriction);
                }
            }
        });
        upRestrictionButton.setAction(new AbstractAction("", GUIUtils.UP_ICON) {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(restrictionsList.getSelectedIndex() > 0); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = restrictionsList.getSelectedIndex();
                if (selIdx > 0) {
                    Collections.swap(restrictionsClone, selIdx, selIdx - 1);
                    restrictionsList.setSelectedIndex(selIdx - 1);
                }
            }
        });
        downRestrictionButton.setAction(new AbstractAction("", GUIUtils.DOWN_ICON) {
            {
                setEnabled(false); // disabled at start
                restrictionsList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(restrictionsList.getSelectedIndex() <
                                restrictionsList.getModel().getSize() - 1); // depends on selection and list length
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = restrictionsList.getSelectedIndex();
                if (selIdx < restrictionsClone.size() - 1) {
                    Collections.swap(restrictionsClone, selIdx, selIdx + 1);
                    restrictionsList.setSelectedIndex(selIdx + 1);
                }
            }
        });

        // listeners
        templatesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Template template = (Template) templatesList.getSelectedValue();
                    if (template != null) {
                        editTemplate(template);
                    }
                }
            }
        });
        restrictionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Restriction restriction = (Restriction) restrictionsList.getSelectedValue();
                    if (restriction != null) {
                        editRestriction(restriction);
                    }
                }
            }
        });

        // clone data
        templatesClone = GlazedLists.eventList(action.getTemplates()); //todo is that really clones members???
        restrictionsClone = GlazedLists.eventList(action.getRestrictions()); //todo is that really clones members???

        // init form data
        nameText.setText(action.getName());
        descriptionText.setText(action.getDescription());
        templatesList.setModel(new DefaultEventListModel<Template>(templatesClone));
        procedureCallCombo.setModel(new DefaultEventComboBoxModel<Procedure>(storyDataHelper.getProcedures()));
        procedureCallCombo.setSelectedItem(action.getProcedureCall().getProcedure());
        restrictionsList.setModel(new DefaultEventListModel<Restriction>(restrictionsClone));
    }

    private boolean editRestriction(Restriction restriction) {
        RestrictionEditor restrictionEditor = new RestrictionEditor(this, restriction, storyDataHelper);
        if (restrictionEditor.showDialog()) {
            try {
                restrictionEditor.updateData(restriction);
                return true;
            } catch (IFML2EditorException e) {
                GUIUtils.showErrorMessage(this, e);
            }
        }
        return false;
    }

    private boolean editTemplate(@NotNull Template template) {
        Procedure selectedProcedure = (Procedure) procedureCallCombo.getSelectedItem();
        TemplateEditor templateEditor = new TemplateEditor(this, template, selectedProcedure);
        if (templateEditor.showDialog()) {
            try {
                templateEditor.updateData(template);
                return true;
            } catch (IFML2EditorException e) {
                GUIUtils.showErrorMessage(this, e);
            }
        }
        return false;
    }

    @Override
    public void updateData(@NotNull Action data) throws IFML2EditorException {
        data.setName(nameText.getText());
        data.setDescription(descriptionText.getText());

        EventList<Template> templates = data.getTemplates();
        templates.clear();
        templates.addAll(templatesClone);

        data.getProcedureCall().setProcedure((Procedure) procedureCallCombo.getSelectedItem());

        data.setRestrictions(restrictionsClone);
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (nameText.getText().trim().length() == 0) {
            throw new DataNotValidException("У действия должно быть имя!", nameText);
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
        contentPane.setPreferredSize(new Dimension(800, 600));
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
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        panel3.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(4, 2, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Основные свойства", panel4);
        final JLabel label1 = new JLabel();
        label1.setText("Название");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameText = new JTextField();
        panel4.add(nameText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Описание");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        descriptionText = new JTextField();
        panel4.add(descriptionText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Вызываемая процедура");
        panel4.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        procedureCallCombo = new JComboBox();
        panel4.add(procedureCallCombo, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(357, 128), null, 0, false));
        templatesList = new JList();
        scrollPane1.setViewportView(templatesList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setOpaque(false);
        toolBar1.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        panel5.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addTemplateButton = new JButton();
        addTemplateButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Add24.gif")));
        addTemplateButton.setText("");
        toolBar1.add(addTemplateButton);
        editTemplateButton = new JButton();
        editTemplateButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Edit24.gif")));
        editTemplateButton.setText("");
        toolBar1.add(editTemplateButton);
        delTemplateButton = new JButton();
        delTemplateButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Delete24.gif")));
        delTemplateButton.setText("");
        toolBar1.add(delTemplateButton);
        final JLabel label4 = new JLabel();
        label4.setText("Шаблоны");
        panel4.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 2, new Insets(4, 4, 4, 4), -1, -1));
        tabbedPane1.addTab("Ограничения", panel6);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel6.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        restrictionsList = new JList();
        scrollPane2.setViewportView(restrictionsList);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        toolBar2.setOpaque(false);
        toolBar2.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        panel6.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addRestrictionButton = new JButton();
        addRestrictionButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Add24.gif")));
        addRestrictionButton.setText("");
        toolBar2.add(addRestrictionButton);
        editRestrictionButton = new JButton();
        editRestrictionButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Edit24.gif")));
        editRestrictionButton.setText("");
        toolBar2.add(editRestrictionButton);
        delRestrictionButton = new JButton();
        delRestrictionButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Delete24.gif")));
        delRestrictionButton.setText("");
        toolBar2.add(delRestrictionButton);
        final JToolBar toolBar3 = new JToolBar();
        toolBar3.setFloatable(false);
        toolBar3.setOrientation(1);
        toolBar3.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        panel6.add(toolBar3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        upRestrictionButton = new JButton();
        upRestrictionButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Up24.gif")));
        upRestrictionButton.setText("");
        toolBar3.add(upRestrictionButton);
        downRestrictionButton = new JButton();
        downRestrictionButton.setIcon(new ImageIcon(getClass().getResource("/ifml2/editor/gui/images/Down24.gif")));
        downRestrictionButton.setText("");
        toolBar3.add(downRestrictionButton);
        label1.setLabelFor(nameText);
        label2.setLabelFor(descriptionText);
        label3.setLabelFor(procedureCallCombo);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
