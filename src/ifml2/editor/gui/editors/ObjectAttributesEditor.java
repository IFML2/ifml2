package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.om.Attribute;
import ifml2.om.Library;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

public class ObjectAttributesEditor extends AbstractEditor<EventList<Attribute>> {
    private static final String OBJECT_ATTRIBUTES_EDITOR_TITLE = "Признаки";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable allAttrsTable;
    private JList<Attribute> objAttrsList;
    private JButton addButton;
    private JButton delButton;
    private EventList<Attribute> attributesClone = null;

    ObjectAttributesEditor(Window owner, @NotNull EventList<Attribute> attributes, @NotNull Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(OBJECT_ATTRIBUTES_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // -- init form --

        addButton.setAction(new ButtonAction(addButton, false) {
            @Override
            public void init() {
                allAttrsTable.getSelectionModel().addListSelectionListener(e -> setEnabled(e.getFirstIndex() >= 0));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                addAttribute();
            }
        });
        allAttrsTable.addMouseListener(new MouseAdapter() {
            // double click adds
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addAttribute();
                }
            }
        });
        delButton.setAction(new ButtonAction(delButton, objAttrsList) {
            @Override
            public void actionPerformed(ActionEvent e) {
                delAttribute();
            }
        });
        objAttrsList.addMouseListener(new MouseAdapter() {
            // double click deletes
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    delAttribute();
                }
            }
        });

        // --- set data with listeners ---

        // set attributes
        attributesClone = GlazedLists.eventList(attributes); // copy refs
        objAttrsList.setModel(new DefaultEventListModel<>(attributesClone));

        // set all attributes (static table model!)
        Vector<Vector<Object>> data = new Vector<>();
        //TODO: iterate through story attributes
        // iterate through libs attributes
        for (Library library : storyDataHelper.getLibraries()) {
            for (Attribute attribute : library.attributes) {
                Vector<Object> line = new Vector<>();
                line.add("<html><i>" + library.getName() + "</i></html>");
                line.add(attribute);
                line.add(attribute.getDescription());
                data.add(line);
            }
        }
        allAttrsTable.setModel(new DefaultTableModel(data, new Vector<>(Arrays.asList("Библиотека", "Признак", "Описание"))) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private void delAttribute() {
        Attribute selectedObject = objAttrsList.getSelectedValue();

        if (selectedObject != null) {
            attributesClone.remove(selectedObject);
        }
    }

    private void addAttribute() {
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

    @Override
    public void updateData(@NotNull EventList<Attribute> attributes) {
        attributes.clear();
        attributes.addAll(attributesClone);
    }
}
