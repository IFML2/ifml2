package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.editors.*;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.engine.EngineVersion;
import ifml2.om.Action;
import ifml2.om.*;
import ifml2.players.guiplayer.GUIPlayer;
import ifml2.tests.gui.TestRunner;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

public class Editor extends JFrame
{
    private static final String FILE_MENU_NAME = "Файл";
    private static final String STORY_MENU_NAME = "История";
    private static final String OPEN_STORY_ACTION_NAME = "Открыть...";
    private static final Logger LOG = Logger.getLogger(Editor.class);
    private JPanel mainPanel;
    private JProgressBar progressBar;
    private ListEditForm<Procedure> proceduresListEditForm;
    private ListEditForm<Location> locationsListEditForm;
    private ListEditForm<Item> itemsListEditForm;
    private ListEditForm<Action> actionsListEditForm;
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

    private boolean editProcedure(@Nullable Procedure procedure)
    {
        if (procedure != null)
        {
            try
            {
                ProcedureEditor procedureEditor = new ProcedureEditor(this, procedure, story.getDataHelper());
                if (procedureEditor.showDialog())
                {
                    procedureEditor.getData(procedure);
                    return true;
                }
            }
            catch (IFML2EditorException e)
            {
                GUIUtils.showErrorMessage(this, e);
            }
        }

        return false;
    }

    private boolean editAction(@Nullable Action action) throws IFML2EditorException
    {
        if (action != null)
        {
            ActionEditor actionEditor = new ActionEditor(this, action, story.getDataHelper());
            if (actionEditor.showDialog())
            {
                actionEditor.getData(action);
                return true;
            }
        }

        return false;
    }

    public void setStory(Story story)
    {
        this.story = story;
        setStoryEdited(false); // reset edited flag
        bindData();
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
        String editorTitle = MessageFormat.format("ЯРИЛ 2.0 Редактор {0} -- {1}{2}", EngineVersion.VERSION, fileName,
                isStoryEdited ? " - * история не сохранена" : "");
        setTitle(editorTitle);
    }

    private int askAboutSavingStory()
    {
        return JOptionPane.showConfirmDialog(Editor.this, "Вы хотите сохранить историю перед выходом?", "История не сохранена",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    private void bindData()
    {
        locationsListEditForm.bindData(story.getLocations());
        itemsListEditForm.bindData(story.getItems());
        proceduresListEditForm.bindData(story.getProcedures());
        actionsListEditForm.bindData(story.getActions());
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
                return f.isDirectory() ? GUIUtils.DIRECTORY_ICON : GUIUtils.STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            return storyFileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    private boolean editLocation(@Nullable Location location)
    {
        if (location != null)
        {
            LocationEditor locationEditor = new LocationEditor(this, location, story.getDataHelper());
            if (locationEditor.showDialog())
            {
                locationEditor.getData(location);
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

    private boolean editItem(@Nullable Item item)
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

    private void createUIComponents()
    {
        locationsListEditForm = new ListEditForm<Location>(this, "локацию", "локации", Word.GenderEnum.FEMININE, Location.class)
        {
            @Override
            protected void addElementToList(Location location)
            {
                if (location != null)
                {
                    story.addLocation(location);
                }
            }

            {
                addListChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        markStoryEdited();
                    }
                });
            }

            @Override
            protected Location createElement() throws Exception
            {
                Location location = new Location();
                return editLocation(location) ? location : null;
            }

            @Override
            protected boolean editElement(Location selectedElement)
            {
                return editLocation(selectedElement);
            }


            //todo warn that location will be deleted from items
        };

        itemsListEditForm = new ListEditForm<Item>(this, "предмет", "предмета", Word.GenderEnum.MASCULINE, Item.class)
        {
            @Override
            protected void addElementToList(Item item)
            {
                if (item != null)
                {
                    story.addItem(item);
                }
            }

            {
                addListChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        markStoryEdited();
                    }
                });
            }

            @Override
            protected Item createElement() throws Exception
            {
                Item item = new Item();
                return editItem(item) ? item : null;
            }

            @Override
            protected boolean editElement(Item selectedElement)
            {
                return editItem(selectedElement);
            }


        };

        proceduresListEditForm = new ListEditForm<Procedure>(this, "процедуру", "процедуры", Word.GenderEnum.FEMININE, Procedure.class)
        {
            @Override
            protected Procedure createElement() throws Exception
            {
                Procedure procedure = new Procedure();
                return editProcedure(procedure) ? procedure : null;
            }

            {
                addListChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        markStoryEdited();
                    }
                });
            }

            @Override
            protected boolean editElement(Procedure selectedElement)
            {
                return editProcedure(selectedElement);
            }

            @Override
            protected boolean beforeDelete(Procedure selectedElement) throws Exception
            {
                if (selectedElement != null)
                {
                    // search for usages in actions
                    ArrayList<Action> affectedActionsList = story.getDataHelper().findActionsByProcedure(selectedElement);
                    if (affectedActionsList.size() > 0)
                    {
                        String message = MessageFormat
                                .format("Это процедура вызывается в действиях:\n{0}\n" + "Поэтому она не может быть удалена.",
                                        affectedActionsList.toString());
                        JOptionPane.showMessageDialog(Editor.this, message, "Процедура используется", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    // check for usage as started procedure
                    if (selectedElement.equals(story.getStoryOptions().getStartProcedureOption().getProcedure()))
                    {
                        int answer = JOptionPane.showConfirmDialog(Editor.this,
                                "Эта процедура установлена как стартовая. Всё равно удалить?\n" +
                                "В этом случае стартовая процедура будет сброшена.", "Процедура используется", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        return answer == JOptionPane.YES_OPTION;
                    }

                    return super.beforeDelete(selectedElement); // do standard asking
                }

                return false;
            }


        };

        actionsListEditForm = new ListEditForm<Action>(this, "действие", "действия", Word.GenderEnum.NEUTER, Action.class)
        {
            @Override
            protected Action createElement() throws Exception
            {
                Action action = new Action();
                return editAction(action) ? action : null;
            }

            {
                addListChangeListener(new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        markStoryEdited();
                    }
                });
            }

            @Override
            protected boolean editElement(Action selectedElement) throws Exception
            {
                return editAction(selectedElement);
            }


        };
    }
}