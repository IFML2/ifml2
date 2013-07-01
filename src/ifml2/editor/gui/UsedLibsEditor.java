package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.om.Library;
import ifml2.om.OMManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class UsedLibsEditor extends JDialog
{
    public static final String USEDLIBS_EDITOR_TITLE = "Используемые библиотеки";
    private JPanel contentPane;
    private JButton buttonOK;
    private JList usedLibsList;
    private JButton addButton;
    private JButton delButton;

    private List<Library> libraries; // todo rewrite using transactional model

    private boolean isOk = false;

    public UsedLibsEditor(Window owner, EventList<Library> libraries)
    {
        super(owner, USEDLIBS_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });

        // call onOK() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onOK();
            }
        });

        // call onOK() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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
                catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(UsedLibsEditor.this, "Произошла ошибка во время загрузки библиотеки: \n"
                            + ex.getMessage(), "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                Library library;
                try
                {
                    library = OMManager.loadLibrary(relativePath);
                    UsedLibsEditor.this.libraries.add(library);
                }
                catch (IFML2Exception error)
                {
                    JOptionPane.showMessageDialog(UsedLibsEditor.this, "Произошла ошибка во время загрузки библиотеки: \n" +
                            error.getMessage(), "Ошибка при загрузке", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                setAllData(UsedLibsEditor.this.libraries);

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
                        UsedLibsEditor.this.libraries.remove(usedLib);
                        setAllData(UsedLibsEditor.this.libraries);
                    }
                }
            }
        });

        // load data in form
        setAllData(libraries);
    }

    private void onOK()
    {
        isOk = true;
        dispose();
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

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
