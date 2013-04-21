package ifml2.editor.gui;

import ifml2.editor.IFML2EditorException;
import ifml2.om.LiteralTemplateElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class LiteralElementEditor extends AbstractEditor<LiteralTemplateElement>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private static final String EDITOR_TITLE = "Литерал";

    public LiteralElementEditor(Window owner, LiteralTemplateElement element)
    {
        super(owner);
        initializeEditor(EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        //todo
    }

    @Override
    public void getData(@NotNull LiteralTemplateElement data) throws IFML2EditorException
    {
        //todo
    }
}
