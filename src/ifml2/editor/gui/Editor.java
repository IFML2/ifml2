package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.editors.*;
import ifml2.engine.Engine;
import ifml2.om.Action;
import ifml2.om.*;
import ifml2.players.guiplayer.GUIPlayer;
import ifml2.tests.gui.TestRunner;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Editor extends JFrame
{
    private static final String FILE_MENU_NAME = "Файл";
    private static final String STORY_MENU_NAME = "История";
    private static final String OPEN_STORY_ACTION_NAME = "Открыть...";
    private static final Logger LOG = Logger.getLogger(Editor.class);
    private AbstractAction editLocationAction; // initializer moved to constructor due to forward declaration error
    private AbstractAction newLocationAction; // initializer moved to constructor due to forward declaration error
    private AbstractAction delLocationAction; // initializer moved to constructor due to forward declaration error
    private JPanel mainPanel;
    private JList locationsList;
    private JList itemsList;
    private JProgressBar progressBar;
    private JButton newLocButton;
    private JButton delLocButton;
    private JButton editLocButton;
    private JButton newItemButton;
    private JButton editItemButton;
    private JButton delItemButton;
    private JList actionsList;
    private JButton addActionButton;
    private JButton editActionButton;
    private JButton delActionButton;
    private ListEditForm<Procedure> proceduresListEditForm;
    private Story story;
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
                                GUIUtils.ReportError(Editor.this, ex);
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

        // actions
        newLocationAction = new ButtonAction(newLocButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Location location = new Location();
                if (editLocation(location))
                {
                    story.addLocation(location);
                    locationsList.setSelectedValue(location, true);
                }
            }
        };
        editLocationAction = new ButtonAction(editLocButton, false)
        {
            @Override
            public void init()
            {
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
        delLocationAction = new ButtonAction(delLocButton, false)
        {
            @Override
            public void init()
            {
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
                    }
                }
            }
        };

        newItemButton.setAction(new ButtonAction(newItemButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Item item = new Item();
                if (editItem(item))
                {
                    story.addItem(item);
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
                    }
                }
            }
        });

        addActionButton.setAction(new ButtonAction(addActionButton)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Action action = new Action();
                try
                {
                    if (editAction(action))
                    {
                        story.getActions().add(action);
                        actionsList.setSelectedValue(action, true);
                    }
                }
                catch (IFML2EditorException ex)
                {
                    GUIUtils.showErrorMessage(Editor.this, ex);
                }
            }
        });
        editActionButton.setAction(new ButtonAction(editActionButton, false)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Action action = (Action) actionsList.getSelectedValue();
                try
                {
                    editAction(action);
                }
                catch (IFML2EditorException ex)
                {
                    GUIUtils.showErrorMessage(Editor.this, ex);
                }
            }

            @Override
            public void init()
            {
                actionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!actionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }
        });
        delActionButton.setAction(new ButtonAction(delActionButton, false)
        {
            @Override
            public void init()
            {
                actionsList.addListSelectionListener(new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        setEnabled(!actionsList.isSelectionEmpty()); // depends on selection
                    }
                });
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Action action = (Action) actionsList.getSelectedValue();
                if (action != null)
                {
                    if (GUIUtils.showDeleteConfirmDialog(Editor.this, "действие", "действия", Word.GenderEnum.NEUTER))
                    {
                        story.getActions().remove(action);
                        markStoryEdited();
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

        actionsList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    Action action = (Action) actionsList.getSelectedValue();
                    if (action != null)
                    {
                        try
                        {
                            editAction(action);
                            actionsList.setSelectedValue(action, true);
                        }
                        catch (IFML2EditorException ex)
                        {
                            GUIUtils.showErrorMessage(Editor.this, ex);
                        }
                    }
                }
            }
        });

        // create new story at start
        setStory(new Story());
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

    private boolean editProcedure(@NotNull Procedure procedure)
    {
        try
        {
            ProcedureEditor procedureEditor = new ProcedureEditor(this, procedure, story.getDataHelper());

            if (procedureEditor.showDialog())
            {
                procedureEditor.getData(procedure);
                markStoryEdited();
                return true;
            }
        }
        catch (IFML2EditorException e)
        {
            GUIUtils.showErrorMessage(this, e);
        }

        return false;
    }

    private boolean editAction(Action action) throws IFML2EditorException
    {
        if (action != null)
        {
            ActionEditor actionEditor = new ActionEditor(this, action, story.getDataHelper());
            if (actionEditor.showDialog())
            {
                actionEditor.getData(action);
                markStoryEdited();
                return true;
            }
        }

        return false;
    }

    public void setStory(Story story)
    {
        this.story = story;
        setStoryEdited(false); // reset edited flag
        loadStoryInForm();
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
        String editorTitle = MessageFormat.format("ЯРИЛ 2.0 Редактор {0} -- {1}{2}", Engine.ENGINE_VERSION, fileName,
                isStoryEdited ? " - * история не сохранена" : "");
        setTitle(editorTitle);
    }

    private int askAboutSavingStory()
    {
        return JOptionPane.showConfirmDialog(Editor.this, "Вы хотите сохранить историю перед выходом?", "История не сохранена",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    private void loadStoryInForm()
    {
        locationsList.setModel(new DefaultEventListModel<Location>(story.getLocations()));

        itemsList.setModel(new DefaultEventListModel<Item>(story.getItems()));

        proceduresListEditForm.init(Editor.this, story.getProcedures(), "процедуру", "процедуры", Word.GenderEnum.FEMININE, true,
                new Callable<Procedure>()
                {
                    @Override
                    public Procedure call() throws Exception
                    {
                        Procedure procedure = new Procedure();
                        if (editProcedure(procedure))
                        {
                            return procedure;
                        }
                        return null;
                    }
                }, new Callable<Boolean>()
                {
                    @Override
                    public Boolean call() throws Exception
                    {
                        Procedure procedure = proceduresListEditForm.getSelectedElement();
                        return procedure != null && editProcedure(procedure);
                    }
                }, new Callable<Boolean>()
                {

                    @Override
                    public Boolean call() throws Exception
                    {
                        Procedure procedure = proceduresListEditForm.getSelectedElement();
                        if (procedure != null)
                        {
                            boolean toDelete = true;

                            // search for usages in actions
                            ArrayList<Action> affectedActionsList = story.getDataHelper().findActionsByProcedure(procedure);
                            if (affectedActionsList.size() > 0)
                            {
                                String message = MessageFormat
                                        .format("Это процедура вызывается в действиях:\n{0}\n" + "Поэтому она не может быть удалена.",
                                                affectedActionsList.toString());
                                JOptionPane.showMessageDialog(Editor.this, message, "Процедура используется", JOptionPane.WARNING_MESSAGE);
                                return false;
                            }

                            // check for usage as started procedure
                            if (procedure.equals(story.getStoryOptions().getStartProcedureOption().getProcedure()))
                            {
                                int answer = JOptionPane
                                        .showConfirmDialog(Editor.this, "Эта процедура установлена как стартовая. Всё равно удалить?\n" +
                                                                        "В этом случае стартовая процедура будет сброшена.",
                                                "Процедура используется", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (answer != JOptionPane.YES_OPTION)
                                {
                                    return false;
                                }
                            }
                            else
                            {
                                // final answer
                                toDelete = proceduresListEditForm.showDeleteConfirmDialog();
                            }

                            if (toDelete)
                            {
                                story.getProcedures().remove(procedure);
                                return true;
                            }
                        }

                        return false;
                    }
                });

        actionsList.setModel(new DefaultEventListModel<Action>(story.getActions()));
    }

    private void loadStory(final String storyFile)
    {
        new Thread() // todo remake to SwingWorker
        {
            @Override
            public void run()
            { //todo rewrite using SwingWorker
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
                    GUIUtils.ReportError(Editor.this, e);
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
                    GUIUtils.ReportError(Editor.this, ex);
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
                        GUIUtils.ReportError(Editor.this, ex);
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
                StoryOptionsEditor storyOptionsEditor = new StoryOptionsEditor(Editor.this, story.getStoryOptions(), story.getDataHelper());
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
                        GUIUtils.ReportError(Editor.this, ex);
                    }
                    markStoryEdited();
                }
            }
        });
        //storyMenu.add(new EditDictAction()); //https://www.hostedredmine.com/issues/11947
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
                    GUIUtils.ReportError(Editor.this, ex);
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
                markStoryEdited();
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