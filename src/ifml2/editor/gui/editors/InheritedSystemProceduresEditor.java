package ifml2.editor.gui.editors;

import ca.odell.glazedlists.CompositeList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.InheritedSystemProcedures;
import ifml2.om.Procedure;
import ifml2.om.Story;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InheritedSystemProceduresEditor extends AbstractEditor<InheritedSystemProcedures>
{
    private static final String INHERITED_SYSTEM_PROCEDURES_EDITOR_FORM_NAME = "Перехваты системных процедур";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox parseErrorHandlerCombo;

    public InheritedSystemProceduresEditor(Window owner, InheritedSystemProcedures inheritedSystemProcedures,
            final Story.DataHelper storyDataHelper)
    {
        super(owner);
        initializeEditor(INHERITED_SYSTEM_PROCEDURES_EDITOR_FORM_NAME, contentPane, buttonOK, buttonCancel);

        // clone data

        // -- init form --

        CompositeList<Procedure> proceduresWithNull = new CompositeList<Procedure>()
        {
            {
                EventList<Procedure> emptyMemberList = createMemberList();
                emptyMemberList.add(null);
                addMemberList(emptyMemberList);

                EventList<Procedure> proceduresMemberList = createMemberList();
                proceduresMemberList.addAll(storyDataHelper.getProcedures());
                addMemberList(proceduresMemberList);
            }
        };

        parseErrorHandlerCombo.setModel(new DefaultEventComboBoxModel<Procedure>(proceduresWithNull));
        parseErrorHandlerCombo.setSelectedItem(inheritedSystemProcedures.getParseErrorHandler());
    }

    @Override
    public void getData(@NotNull InheritedSystemProcedures data) throws IFML2EditorException
    {
        data.setParseErrorHandler((Procedure) parseErrorHandlerCombo.getSelectedItem());
    }
}
