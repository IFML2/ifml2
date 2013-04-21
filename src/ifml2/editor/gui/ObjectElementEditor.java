package ifml2.editor.gui;

import ifml2.editor.IFML2EditorException;
import ifml2.om.ObjectTemplateElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ObjectElementEditor extends AbstractEditor<ObjectTemplateElement>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private static final String EDITOR_TITLE = "Объект";

    public ObjectElementEditor(Window owner, ObjectTemplateElement element)
    {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        //todo
    }

    @Override
    public void getData(@NotNull ObjectTemplateElement data) throws IFML2EditorException
    {
        //todo
    }
}
