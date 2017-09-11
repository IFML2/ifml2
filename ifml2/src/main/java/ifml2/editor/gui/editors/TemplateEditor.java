package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.LiteralTemplateElement;
import ifml2.om.ObjectTemplateElement;
import ifml2.om.Procedure;
import ifml2.om.Template;
import ifml2.om.TemplateElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

import static ifml2.om.Word.Gender.MASCULINE;

public class TemplateEditor extends AbstractEditor<Template> {
    private static final String EDITOR_TITLE = "Шаблон";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList templateList;
    private JButton addButton;
    private JButton editButton;
    private JButton delButton;
    private JButton upButton;
    private JButton downButton;
    private Template templateClone;
    private Procedure procedure;

    public TemplateEditor(Window owner, @NotNull Template template, Procedure procedure) {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        this.procedure = procedure;

        // init actions
        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object literalOption = new Object() {
                    @Override
                    public String toString() {
                        return "Литерал";
                    }
                };
                Object objectOption = new Object() {
                    @Override
                    public String toString() {
                        return "Объект";
                    }
                };
                Object answer = JOptionPane.showInputDialog(TemplateEditor.this, "Выберите тип элемента шаблона:", "Тип элемента шаблона",
                        JOptionPane.QUESTION_MESSAGE, null, new Object[]{literalOption, objectOption}, literalOption);

                TemplateElement element = null;

                if (literalOption.equals(answer)) {
                    element = new LiteralTemplateElement();
                } else if (objectOption.equals(answer)) {
                    element = new ObjectTemplateElement();
                }

                try {
                    if (element != null && editElement(element)) {
                        templateClone.getElements().add(element);
                        templateList.setSelectedValue(element, true);
                    }
                } catch (IFML2EditorException ex) {
                    GUIUtils.showErrorMessage(TemplateEditor.this, ex);
                }
            }
        });
        editButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                templateList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!templateList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                TemplateElement selectedElement = (TemplateElement) templateList.getSelectedValue();
                if (selectedElement != null) {
                    try {
                        editElement(selectedElement);
                    } catch (IFML2EditorException ex) {
                        GUIUtils.showErrorMessage(TemplateEditor.this, ex);
                    }
                }
            }
        });
        delButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            {
                setEnabled(false); // disabled at start
                templateList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(!templateList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                TemplateElement selectedElement = (TemplateElement) templateList.getSelectedValue();
                if (selectedElement != null) {
                    if (GUIUtils.showDeleteConfirmDialog(TemplateEditor.this, "шаблон", "шаблона", MASCULINE)) {
                        templateClone.getElements().remove(selectedElement);
                    }
                }
            }
        });
        upButton.setAction(new AbstractAction("", GUIUtils.UP_ICON) {
            {
                setEnabled(false); // disabled at start
                templateList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(templateList.getSelectedIndex() > 0); // depends on selection and list length
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = templateList.getSelectedIndex();
                if (selIdx > 0) {
                    Collections.swap(templateClone.getElements(), selIdx, selIdx - 1);
                    templateList.setSelectedIndex(selIdx - 1);
                }
            }
        });
        downButton.setAction(new AbstractAction("", GUIUtils.DOWN_ICON) {
            {
                setEnabled(false); // disabled at start
                templateList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        setEnabled(templateList.getSelectedIndex() < templateList.getModel().getSize() - 1); // depends on selection and list length
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int selIdx = templateList.getSelectedIndex();
                EventList<TemplateElement> instructions = templateClone.getElements();
                if (selIdx < instructions.size() - 1) {
                    Collections.swap(instructions, selIdx, selIdx + 1);
                    templateList.setSelectedIndex(selIdx + 1);
                }
            }
        });

        // listeners
        templateList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TemplateElement selectedElement = (TemplateElement) templateList.getSelectedValue();
                    if (selectedElement != null) {
                        try {
                            editElement(selectedElement);
                        } catch (IFML2EditorException ex) {
                            GUIUtils.showErrorMessage(TemplateEditor.this, ex);
                        }
                    }
                }
            }
        });

        // elements
        try {
            // clone data
            templateClone = template.clone();
            // load data
            templateList.setModel(new DefaultEventListModel<TemplateElement>(templateClone.getElements()));
        } catch (CloneNotSupportedException e) {
            GUIUtils.showErrorMessage(this, e);
        }

    }

    private boolean editElement(@NotNull TemplateElement element) throws IFML2EditorException {
        if (element instanceof LiteralTemplateElement) {
            LiteralTemplateElement literalElement = (LiteralTemplateElement) element;
            LiteralElementEditor literalElementEditor = new LiteralElementEditor(this, literalElement, procedure);
            if (literalElementEditor.showDialog()) {
                literalElementEditor.updateData(literalElement);
                return true;
            }
        } else if (element instanceof ObjectTemplateElement) {
            ObjectTemplateElement objectElement = (ObjectTemplateElement) element;
            ObjectElementEditor objectElementEditor = new ObjectElementEditor(this, objectElement, procedure);
            if (objectElementEditor.showDialog()) {
                objectElementEditor.updateData(objectElement);
                return true;
            }
        } else {
            throw new IFML2EditorException("Неизвестный тип элемента шаблона: {0}", element.getClass());
        }
        return false;
    }

    @Override
    protected void validateData() throws DataNotValidException {
        if (templateClone.getSize() == 0) {
            throw new DataNotValidException("В шаблоне нет элементов!", templateList);
        }
    }

    @Override
    public void updateData(@NotNull Template data) throws IFML2EditorException {
        data.setElements(templateClone.getElements());
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
        contentPane.setPreferredSize(new Dimension(640, 480));
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
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel3.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(466, 20), null, 0, false));
        addButton = new JButton();
        addButton.setText("Добавить...");
        toolBar1.add(addButton);
        editButton = new JButton();
        editButton.setText("Редактировать...");
        toolBar1.add(editButton);
        delButton = new JButton();
        delButton.setText("Удалить...");
        toolBar1.add(delButton);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(466, 128), null, 0, false));
        templateList = new JList();
        templateList.setSelectionMode(0);
        scrollPane1.setViewportView(templateList);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        toolBar2.setOrientation(1);
        toolBar2.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        panel3.add(toolBar2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        upButton = new JButton();
        upButton.setText("Button");
        toolBar2.add(upButton);
        downButton = new JButton();
        downButton.setText("Button");
        toolBar2.add(downButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
