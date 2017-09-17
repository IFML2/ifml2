package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Attribute;
import ifml2.om.Library;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Vector;

public class ObjectAttributesEditor extends AbstractEditor<EventList<Attribute>> {
    public static final String OBJECT_ATTRIBUTES_EDITOR_TITLE = "Признаки";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable allAttrsTable;
    private JList objAttrsList;
    private JButton addButton;
    private JButton delButton;
    private EventList<Attribute> attributesClone = null;

    public ObjectAttributesEditor(Window owner, @NotNull EventList<Attribute> attributes, @NotNull Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(OBJECT_ATTRIBUTES_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- init form --

        addButton.setAction(new AbstractAction("", GUIUtils.MOVE_LEFT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = allAttrsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Attribute attribute = (Attribute) allAttrsTable.getValueAt(selectedRow, 1);

                    int attributeIndex = attributesClone.indexOf(attribute);
                    if (attributeIndex >= 0) {
                        objAttrsList.setSelectedIndex(attributeIndex); // just highlight existing attribute w/o adding
                    } else {
                        attributesClone.add(attribute);
                        objAttrsList.setSelectedValue(attribute, true);
                    }
                }
            }
        });
        delButton.setAction(new AbstractAction("", GUIUtils.MOVE_RIGHT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Attribute selectedObject = (Attribute) objAttrsList.getSelectedValue();

                if (selectedObject != null) {
                    attributesClone.remove(selectedObject);
                }
            }
        });

        // --- set data with listeners ---

        // set attributes
        attributesClone = GlazedLists.eventList(attributes);
        objAttrsList.setModel(new DefaultEventListModel<Attribute>(attributesClone));

        // set all attributes (static table model!)
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        //TODO: iterate through story attributes
        // iterate through libs attributes
        for (Library library : storyDataHelper.getLibraries()) {
            for (Attribute attribute : library.attributes) {
                Vector<Object> line = new Vector<Object>();
                line.add("<html><i>" + library.getName() + "</i></html>");
                line.add(attribute);
                line.add(attribute.getDescription());
                data.add(line);
            }
        }
        allAttrsTable.setModel(new DefaultTableModel(data, new Vector<String>(Arrays.asList("Библиотека", "Признак", "Описание"))) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    @Override
    public void updateData(@NotNull EventList<Attribute> attributes) {
        attributes.clear();
        attributes.addAll(attributesClone);
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

}
