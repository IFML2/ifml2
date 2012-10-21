package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.GUIUtils;
import ifml2.om.Attribute;
import ifml2.om.Library;
import ifml2.om.Story;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Vector;

public class ObjectAttributesEditor extends JDialog
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable allAttrsTable;
    private JList objAttrsList;
    private JButton addButton;
    private JButton delButton;

    private EventList<Attribute> attributesClone = null;
    public boolean isOk = false;

    public ObjectAttributesEditor(EventList<Attribute> attributes, Story story)
    {
        assert attributes != null;
        assert story != null;

        setTitle("Признаки");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onOK();}
        });

        buttonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {onCancel();}
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        AddAttributeAction addAttributeAction = new AddAttributeAction();
        addButton.setAction(addAttributeAction);
        DelAttributeAction delAttributeAction = new DelAttributeAction();
        delButton.setAction(delAttributeAction);

        // --- set data with listeners ---

        // set attributes
        attributesClone = GlazedLists.eventList(attributes);
        objAttrsList.setModel(new DefaultEventListModel<Attribute>(attributesClone));

        // set all attributes (static table model!)
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        //TODO: iterate through story attributes
        // iterate through libs attributes
        for(Library library : story.libraries)
        {
            for(Attribute attribute : library.attributes)
            {
                Vector<Object> line = new Vector<Object>();
                line.add("<html><i>" + library.getName() + "</i></html>");
                line.add(attribute);
                line.add(attribute.getDescription());
                data.add(line);
            }
        }
        TableModel tableModel = new DefaultTableModel(data, new Vector<String>(Arrays.asList("Библиотека", "Признак", "Описание"))) {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        allAttrsTable.setModel(tableModel);
    }

    private void onOK()
    {
        isOk = true;
        dispose();
    }

    private void onCancel()
    {
        isOk = false;
        dispose();
    }

    public void getData(EventList<Attribute> attributes)
    {
        attributes.clear();
        attributes.addAll(attributesClone);
    }

    private class AddAttributeAction extends AbstractAction
    {
        private AddAttributeAction()
        {
            super("<<");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int selectedRow = allAttrsTable.getSelectedRow();
            if(selectedRow >= 0)
            {
                Attribute attribute = (Attribute) allAttrsTable.getValueAt(selectedRow, 1);
                
                int attributeIndex = attributesClone.indexOf(attribute);
                if(attributeIndex >= 0)
                {
                    objAttrsList.setSelectedIndex(attributeIndex); // just highlight existing attribute w/o adding
                }
                else
                {
                    attributesClone.add(attribute);
                    objAttrsList.setSelectedValue(attribute, true);
                }
            }
        }
    }

    private class DelAttributeAction extends AbstractAction
    {
        private DelAttributeAction()
        {
            super(">>");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Attribute selectedObject = (Attribute) objAttrsList.getSelectedValue();

            if(selectedObject != null)
            {
                attributesClone.remove(selectedObject);
            }
        }
    }
}
