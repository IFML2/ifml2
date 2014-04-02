package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.IFML2EditorException;
import ifml2.engine.Engine;
import ifml2.om.Action;
import ifml2.om.*;
import ifml2.players.guiplayer.GUIPlayer;
import ifml2.tests.gui.TestRunner;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.xml.bind.ValidationEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

public class Editor extends JFrame
{
    private static final String FILE_MENU_NAME = "Файл";
    private static final String STORY_MENU_NAME = "История";
    private static final String OPEN_STORY_ACTION_NAME = "Открыть...";
    private static final String EDIT_LOCATION_ACTION_NAME = "Редактировать...";
    private static final Logger LOG = Logger.getLogger(Editor.class);
    private final AbstractAction newLocationAction = new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Location location = new Location();
            if (editLocation(location))
            {
                story.addLocation(location);
                markStoryEdited();
                reloadDataInForm();
                locationsList.setSelectedValue(location, true);
            }
        }
    };
    private final AbstractAction editLocationAction = new AbstractAction(EDIT_LOCATION_ACTION_NAME,
                                                                         GUIUtils.EDIT_ELEMENT_ICON)
    {
        {
            setEnabled(false); // disabled at start
            locationsList.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    setEnabled(!locationsList.isSelectionEmpty()); // depends on selection
                }
            });
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            editLocation((Location) locationsList.getSelectedValue());
        }
    };
    private final AbstractAction delLocationAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        {
            setEnabled(false); // disabled at start
            locationsList.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    setEnabled(!locationsList.isSelectionEmpty()); // depends on selection
                }
            });
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Location location = (Location) locationsList.getSelectedValue();
            if (location != null)
            {
                if (GUIUtils.showDeleteConfirmDialog(Editor.this, "локацию", "локации", Word.GenderEnum.FEMININE))
                {
                    story.getLocations().remove(location);
                    markStoryEdited();
                    reloadDataInForm();
                }
            }
        }
    };
    private JPanel mainPanel;
    private JList locationsList;
    private JList itemsList;
    private JProgressBar progressBar;
    private JButton newLocButton;
    private JButton editLocButton;
    private JButton delLocButton;
    private JButton newItemButton;
    private JButton editItemButton;
    private JButton delItemButton;
    private Story story = new Story();
    private boolean isStoryEdited = false;
    private String storyFileName = "новая история";

    public Editor()
    {
        updateTitle();
        setContentPane(mainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (!isStoryEdited)
                {
                    dispose();
                }
                else
                {
                    int answer = askAboutSavingStory();
                    switch (answer)
                    {
                        case JOptionPane.YES_OPTION:
                            try
                            {
                                selectFileAndSaveStory();
                            }
                            catch (IFML2Exception ex)
                            {
                                GUIUtils.showErrorMessage(Editor.this, ex);
                            }
                            break;
                        case JOptionPane.NO_OPTION:
                            dispose();
                            break;
                    }
                }
            }
        });

        setJMenuBar(createMainMenu());

        GUIUtils.packAndCenterWindow(this);

        newLocButton.setAction(newLocationAction);
        editLocButton.setAction(editLocationAction);
        delLocButton.setAction(delLocationAction);

        newItemButton.setAction(new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = new Item();
                if (editItem(item))
                {
                    story.addItem(item);
                    markStoryEdited();
                    reloadDataInForm();
                    itemsList.setSelectedValue(item, true);
                }
            }
        });
        editItemButton.setAction(new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                itemsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!itemsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = (Item) itemsList.getSelectedValue();
                if (editItem(item))
                {
                    markStoryEdited();
                    reloadDataInForm();
                    itemsList.setSelectedValue(item, true);
                }
            }
        });
        delItemButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            {
                setEnabled(false); // disabled at start
                itemsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!itemsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = (Item) itemsList.getSelectedValue();
                if (item != null)
                {
                    if (GUIUtils.showDeleteConfirmDialog(Editor.this, "предмет", "предмета", Word.GenderEnum.MASCULINE))
                    {
                        story.getItems().remove(item);
                        markStoryEdited();
                        reloadDataInForm();
                    }
                }
            }
        });

        final JPopupMenu locationPopupMenu = createPopupMenus();

        locationsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    locationPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Location location = (Location) locationsList.getSelectedValue();
                    if (location != null)
                    {
                        editLocation(location);
                    }
                }
            }
        });

        itemsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Item item = (Item) itemsList.getSelectedValue();
                    if (item != null)
                    {
                        editItem(item);
                    }
                }
            }
        });
    }

    public static void main(String[] args)
    {
        Editor editor = new Editor();
        editor.setVisible(true);

        if (args != null && args.length >= 1)
        {
            // load story
            editor.loadStory(args[0]);
        }
    }

    public void setStory(Story story)
    {
        this.story = story;
        setStoryEdited(false); // reset edited flag
        reloadDataInForm();
    }

    public void setStoryEdited(boolean storyEdited)
    {
        isStoryEdited = storyEdited;
        updateTitle();
    }

    private void markStoryEdited()
    {
        setStoryEdited(true);
    }

    /**
     * Updates Editor's title - including story filename and modification asterisk (*)
     */
    private void updateTitle()
    {
        File file = new File(storyFileName);
        String fileName = file.getName();
        String editorTitle =
                MessageFormat.format("ЯРИЛ 2.0 Редактор {0} -- {1}{2}", Engine.ENGINE_VERSION, fileName, isStoryEdited ? " - * история не сохранена" : "");
        setTitle(editorTitle);
    }

    private int askAboutSavingStory()
    {
        return JOptionPane
                .showConfirmDialog(Editor.this, "Вы хотите сохранить историю перед выходом?", "История не сохранена",
                                   JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    private void reloadDataInForm()
    {
        // locations
        DefaultListModel locationsListModel = new DefaultListModel();
        for (Location location : story.getLocations())
        {
            locationsListModel.addElement(location);
        }
        locationsList.setModel(locationsListModel);

        // items
        DefaultListModel itemsListModel = new DefaultListModel();
        for (Item item : story.getItems())
        {
            itemsListModel.addElement(item);
        }
        itemsList.setModel(itemsListModel);
    }

    private void loadStory(final String storyFile)
    {
        new Thread() // todo remake to SwingWorker
        {
            @Override
            public void run()
            {
                Cursor previousCursor = mainPanel.getCursor();
                mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                //getContentPane().setEnabled(false);
                try
                {
                    progressBar.setVisible(true);
                    Editor.this.setStory(OMManager.loadStoryFromFile(storyFile, false, false).getStory());
                    Editor.this.setStoryFileName(storyFile);
                }
                catch (Throwable e)
                {
                    LOG.error("Error while loading story!", e);
                    ReportError(e, "Ошибка при загрузке истории!");
                    //GUIUtils.showErrorMessage(Editor.this, e);
                }
                finally
                {
                    //getContentPane().setEnabled(true); // todo try SwingUtilities.invokeLater
                    progressBar.setVisible(false);
                    mainPanel.setCursor(previousCursor);
                }
            }
        }.start();
    }

    private void ReportError(Throwable exception, String dialogTitle)
    {
        exception.printStackTrace();
        LOG.error(dialogTitle, exception);
        String errorMessage = "";
        if(!(exception instanceof IFML2LoadXmlException) && exception.getCause() instanceof IFML2LoadXmlException)
        {
            exception = exception.getCause();
        }
        if (exception instanceof IFML2LoadXmlException)
        {
            errorMessage += "В файле истории есть ошибки:";
            for (ValidationEvent validationEvent : ((IFML2LoadXmlException) exception).getEvents())
            {
                errorMessage += MessageFormat.format("\n\"{0}\" at {1},{2}", validationEvent.getMessage(),
                                                     validationEvent.getLocator().getLineNumber(),
                                                     validationEvent.getLocator().getColumnNumber());
            }
        }
        else
        {
            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            errorMessage += stringWriter.toString();
        }
        GUIUtils.showMemoDialog(this, "Произошла ошибка", errorMessage);
    }

    private JMenuBar createMainMenu()
    {
        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu(FILE_MENU_NAME);
        fileMenu.add(new AbstractAction("Новая история", GUIUtils.NEW_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (isStoryEdited)
                {
                    int answer = askAboutSavingStory();
                    switch (answer)
                    {
                        case JOptionPane.YES_OPTION:
                            try
                            {
                                if (!selectFileAndSaveStory())
                                {
                                    return;
                                }
                            }
                            catch (IFML2Exception ex)
                            {
                                GUIUtils.showErrorMessage(Editor.this, ex);
                            }
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            return;
                    }
                }
                setStory(new Story());
                setStoryFileName("новая история");
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction(OPEN_STORY_ACTION_NAME, GUIUtils.OPEN_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (isStoryEdited)
                {
                    int answer = askAboutSavingStory();
                    switch (answer)
                    {
                        case JOptionPane.YES_OPTION:
                            try
                            {
                                if (!selectFileAndSaveStory())
                                {
                                    return;
                                }
                            }
                            catch (IFML2Exception ex)
                            {
                                GUIUtils.showErrorMessage(Editor.this, ex);
                            }
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            return;
                    }
                }
                String storyFileName = selectStoryFileForOpen();
                if (storyFileName != null)
                {
                    loadStory(storyFileName);
                }
            }
        });
        fileMenu.add(new AbstractAction("Сохранить...", GUIUtils.SAVE_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    selectFileAndSaveStory();
                }
                catch (IFML2Exception ex)
                {
                    JOptionPane.showMessageDialog(Editor.this, "Ошибка во время сохранения истории: " + ex.getMessage());
                    ReportError(ex, "Ошибка во время сохранения истории");
                }
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Экспортировать зашифрованную историю...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // choose cipher story file:
                JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
                storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
                storyFileChooser.setFileFilter(new FileFilter()
                {
                    @Override
                    public String getDescription()
                    {
                        return CommonConstants.CIPHERED_STORY_FILE_FILTER_NAME;
                    }

                    @Override
                    public boolean accept(File file)
                    {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.CIPHERED_STORY_EXTENSION) ||
                               !file.exists();
                    }
                });

                storyFileChooser.setFileView(new FileView()
                {
                    @Override
                    public Icon getIcon(File f)
                    {
                        if (f.isDirectory())
                        {
                            return GUIUtils.DIRECTORY_ICON;
                        }
                        return GUIUtils.CIPHERED_STORY_FILE_ICON;
                    }
                });

                if (storyFileChooser.showSaveDialog(Editor.this) == JFileChooser.APPROVE_OPTION)
                {
                    String fileName = storyFileChooser.getSelectedFile().getAbsolutePath();

                    if (!fileName.toLowerCase().endsWith(CommonConstants.CIPHERED_STORY_EXTENSION))
                    {
                        fileName += CommonConstants.CIPHERED_STORY_EXTENSION;
                    }

                    try
                    {
                        OMManager.exportCipheredStory(fileName, story);
                    }
                    catch (IFML2Exception ex)
                    {
                        ReportError(ex, "Произошла ошибка во время экспорта зашифрованной истории.");
                    }
                }
            }
        });
        mainMenu.add(fileMenu);

        JMenu storyMenu = new JMenu(STORY_MENU_NAME);
        storyMenu.add(new AbstractAction("Настройки истории...", GUIUtils.PREFERENCES_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                StoryOptionsEditor storyOptionsEditor = new StoryOptionsEditor(Editor.this, story.getStoryOptions(),
                                                                               story.getDataHelper());
                if (storyOptionsEditor.showDialog())
                {
                    markStoryEdited();
                    storyOptionsEditor.getData(story.getStoryOptions());
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Используемые библиотеки...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                EventList<Library> libraries = story.getLibraries();
                UsedLibsEditor usedLibsEditor = new UsedLibsEditor(Editor.this, libraries, story.getDataHelper());
                if (usedLibsEditor.showDialog())
                {
                    try
                    {
                        usedLibsEditor.getData(libraries);
                    }
                    catch (Throwable ex)
                    {
                        ReportError(ex, "Ошибка при редактировании списка использованных библиотек");
                    }
                    markStoryEdited();
                }
            }
        });
        //storyMenu.add(new EditDictAction()); //https://www.hostedredmine.com/issues/11947
        storyMenu.add(new AbstractAction("Редактировать процедуры...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ProceduresEditor proceduresEditor = new ProceduresEditor(Editor.this, story.getProcedures(),
                                                                         story.getDataHelper());
                if (proceduresEditor.showDialog())
                {
                    markStoryEdited();
                }
            }
        });
        storyMenu.add(new AbstractAction("Редактировать действия...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                EventList<Action> actions = story.getActions();
                ActionsEditor actionsEditor = new ActionsEditor(Editor.this, story.getDataHelper());
                if (actionsEditor.showDialog())
                {
                    try
                    {
                        actionsEditor.getData(actions);
                    }
                    catch (IFML2EditorException ex)
                    {
                        GUIUtils.showErrorMessage(Editor.this, ex);
                    }
                    markStoryEdited();
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Запустить историю в Плеере...", GUIUtils.PLAY_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String fileName;
                try
                {
                    File tempFile = File.createTempFile("ifml2run_", ".xml");
                    fileName = tempFile.getAbsolutePath();
                    saveStory(fileName, false);
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            GUIPlayer.startFromFile(fileName, true);
                        }
                    });
                }
                catch (Throwable ex)
                {
                    ReportError(ex, "Ошибка во время сохранения истории во временный файл");
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Открыть Тестер...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TestRunner.main(new String[]{});
            }
        });
        mainMenu.add(storyMenu);

        return mainMenu;
    }

    private String selectStoryFileForOpen()
    {
        // choose story file:
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
        storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
        storyFileChooser.setFileFilter(new FileFilter()
        {
            @Override
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

        storyFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                if (f.isDirectory())
                {
                    return GUIUtils.DIRECTORY_ICON;
                }
                return GUIUtils.STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            return storyFileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    private JPopupMenu createPopupMenus()
    {
        // locations popup
        JPopupMenu locationPopupMenu = new JPopupMenu();

        locationPopupMenu.add(newLocationAction);
        locationPopupMenu.addSeparator();
        locationPopupMenu.add(editLocationAction);
        locationPopupMenu.add(delLocationAction);

        return locationPopupMenu;
    }

    /*private class EditDictAction extends AbstractAction
    {
        private EditDictAction()
        {
            super("Редактировать словарь...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            editDict();
        }
    }*/

    /*private void editDict()
    {
        DictionaryEditor dictionaryEditor = new DictionaryEditor(story.dictionary);
        dictionaryEditor.setVisible(true);
    }*/

    private boolean editLocation(Location location)
    {
        if (location != null)
        {
            LocationEditor locationEditor = new LocationEditor(this, location, story.getDataHelper());
            if (locationEditor.showDialog())
            {
                locationEditor.getData(location);
                markStoryEdited();
                return true;
            }
        }

        return false;
    }

    private boolean selectFileAndSaveStory() throws IFML2Exception
    {
        String storyFileName = selectFileForStorySave();
        if (storyFileName != null)
        {
            saveStory(storyFileName, true);
            setStoryFileName(storyFileName);
            return true;
        }

        return false;
    }

    private void saveStory(String storyFileName, boolean toMarkAsSaved) throws IFML2Exception
    {
        OMManager.saveStoryToXmlFile(storyFileName, story);
        if (toMarkAsSaved)
        {
            setStoryEdited(false); // reset edited flag
        }
    }

    private String selectFileForStorySave()
    {
        // choose story file:
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
        storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
        storyFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return CommonConstants.STORY_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File file)
            {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.STORY_EXTENSION) ||
                       !file.exists();
            }
        });

        storyFileChooser.setFileView(new FileView()
        {
            @Override
            public Icon getIcon(File f)
            {
                if (f.isDirectory())
                {
                    return GUIUtils.DIRECTORY_ICON;
                }
                return GUIUtils.STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        String fileName = storyFileChooser.getSelectedFile().getAbsolutePath();

        if (!fileName.toLowerCase().endsWith(CommonConstants.STORY_EXTENSION))
        {
            fileName += CommonConstants.STORY_EXTENSION;
        }

        return fileName;
    }

    private boolean editItem(Item item)
    {
        if (item != null)
        {
            ItemEditor itemEditor = new ItemEditor(this, item, story.getDataHelper());
            if (itemEditor.showDialog())
            {
                itemEditor.getData(item);
                return true;
            }
        }

        return false;
    }

    public void setStoryFileName(String storyFileName)
    {
        this.storyFileName = storyFileName;
        updateTitle();
    }
}