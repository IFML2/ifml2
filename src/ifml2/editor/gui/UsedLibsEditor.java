package ifml2.editor.gui;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Library;
import ifml2.om.OMManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class UsedLibsEditor extends AbstractEditor<List<Library>>
{
    private JPanel contentPane;
    private JButton buttonOK;
    private JList usedLibsList;
    private JButton addButton;
    private JButton delButton;
    private List<Library> libraries;

    public static final String USED_LIBS_EDITOR_TITLE = "Используемые библиотеки";

    public UsedLibsEditor(Window owner)
    {
        super(owner);
        initializeEditor(USED_LIBS_EDITOR_TITLE, contentPane, buttonOK, null);

        // -- nint form --

        addButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser libFileChooser = new JFileChooser(CommonUtils.getLibrariesDirectory());
                libFileChooser.setFileFilter(new FileFilter()
                {
                    public String getDescription()
                    {
                        return CommonConstants.STORY_FILE_FILTER_NAME;
                    }

                    @Override
                    public boolean accept(File file)
                    {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.STORY_EXTENSION);
                    }
                });

                if (libFileChooser.showOpenDialog(UsedLibsEditor.this) != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }

                String relativePath;
                try
                {
                    URI librariesDirectoryURI = new File(CommonUtils.getLibrariesDirectory()).getCanonicalFile().toURI();
                    URI selectedFile = libFileChooser.getSelectedFile().toURI();
                    relativePath = librariesDirectoryURI.relativize(selectedFile).getPath();
                }
                catch (IOException e1)
                {
                    JOptionPane.showMessageDialog(UsedLibsEditor.this, "Произошла ошибка во время загрузки библиотеки: \n"
                            + e1.getMessage(), "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                Library library;
                try
                {
                    library = OMManager.loadLibrary(relativePath);
                    libraries.add(library);
                }
                catch (IFML2Exception error)
                {
                    JOptionPane.showMessageDialog(UsedLibsEditor.this, "Произошла ошибка во время загрузки библиотеки: \n" +
                            error.getMessage(), "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                setAllData(libraries);

                if (library != null)
                {
                    usedLibsList.setSelectedValue(library, true);
                }
            }
        });

        delButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Library usedLib = (Library) usedLibsList.getSelectedValue();
                if(usedLib != null)
                {
                    int answer = JOptionPane.showConfirmDialog(UsedLibsEditor.this,
                            "Вы уверены, что не хотите больше использовать эту библиотеку?", "Удаление библиотеки",
                            JOptionPane.YES_NO_OPTION);
                    if(answer == 0)
                    {
                        libraries.remove(usedLib);
                        setAllData(libraries);
                    }
                }
            }
        });
    }

    @Override
    public void getData(@NotNull List<Library> data) throws IFML2EditorException
    {
        //todo transfer to transact model
    }

    public void setAllData(List<Library> libraries)
    {
        this.libraries = libraries;
        DefaultListModel usedLibsListModel = new DefaultListModel();
        for(Library library : libraries)
        {
            usedLibsListModel.addElement(library);
        }
        usedLibsList.setModel(usedLibsListModel);
    }
}
