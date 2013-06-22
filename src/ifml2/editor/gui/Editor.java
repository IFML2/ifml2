package ifml2.editor.gui;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.OMManager;
import ifml2.om.Story;
import ifml2.players.guiplayer.GUIPlayer;
import ifml2.tests.gui.TestRunner;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;

public class Editor extends JFrame
{
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

    private final AbstractAction newLocationAction = new AbstractAction("Добавить...", GUIUtils.ADD_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Location location = new Location();
            if(editLocation(location))
            {
                story.addLocation(location);
                reloadDataInForm();
                locationsList.setSelectedValue(location, true);
            }
        }
    };
    private final AbstractAction editLocationAction = new AbstractAction(EDIT_LOCATION_ACTION_NAME, GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            editLocation((Location) locationsList.getSelectedValue());
        }
    };
    private final AbstractAction delLocationAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Location location = (Location) locationsList.getSelectedValue();
            if(location != null)
            {
                if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Editor.this, "Вы уверены, что хотите удалить эту локацию?",
                        "Удаление локации", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
                {
                    story.getLocations().remove(location);
                    reloadDataInForm();
                }
            }
        }
    };

    private final AbstractAction editItemAction = new AbstractAction("Редактировать...", GUIUtils.EDIT_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = (Item) itemsList.getSelectedValue();
            if(editItem(item))
            {
                reloadDataInForm();
                itemsList.setSelectedValue(item, true);
            }
        }
    };
    private final AbstractAction delItemAction = new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = (Item) itemsList.getSelectedValue();
            if(item != null)
            {
                if(JOptionPane.showConfirmDialog(Editor.this, "Вы уверены, что хотите удалить этот предмет?",
                        "Удаление предмета", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    story.getItems().remove(item);
                    reloadDataInForm();
                }
            }
        }
    };

    private static final String FILE_MENU_NAME = "Файл";
    private static final String STORY_MENU_NAME = "История";
    private static final String OPEN_STORY_ACTION_NAME = "Открыть...";
    private static final String EDIT_LOCATION_ACTION_NAME = "Редактировать...";
    private static final Logger LOG = Logger.getLogger(Editor.class);

    public Editor()
    {
        String IFML_EDITOR_VERSION = "ЯРИЛ 2.0 Редактор " + Engine.ENGINE_VERSION;
        setTitle(IFML_EDITOR_VERSION);
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
                if(editItem(item))
                {
                    story.addItem(item);
                    reloadDataInForm();
                    itemsList.setSelectedValue(item, true);
                }
            }
        });
        editItemButton.setAction(editItemAction);
        delItemButton.setAction(delItemAction);

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
                if(e.getClickCount() == 2)
                {
                    Location location = (Location) locationsList.getSelectedValue();
                    if(location != null)
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
                if(e.getClickCount() == 2)
                {
                    Item item = (Item) itemsList.getSelectedValue();
                    if(item != null)
                    {
                        editItem(item);
                    }
                }
            }
        });

        locationsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateActions();
            }
        });

        itemsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateActions();
            }
        });

        updateActions();
    }

    private void updateActions()
    {
        Object selectedLoc = locationsList.getSelectedValue();
        editLocationAction.setEnabled(selectedLoc != null);
        delLocationAction.setEnabled(selectedLoc != null);

        Object selectedItem = itemsList.getSelectedValue();
        editItemAction.setEnabled(selectedItem != null);
        delItemAction.setEnabled(selectedItem != null);
    }

    public static void main(String[] args)
    {
        Editor editor = new Editor();
        editor.setVisible(true);

        if(args != null && args.length >= 1)
        {
            // load story
            editor.loadStory(args[0]);
        }
    }

    void setAllData(Story story)
    {
        this.story = story;
        reloadDataInForm();
    }

    private void reloadDataInForm()
    {
        // locations
        DefaultListModel locationsListModel = new DefaultListModel();
        for(Location location : story.getLocations())
        {
            locationsListModel.addElement(location);
        }
        locationsList.setModel(locationsListModel);

        // items
        DefaultListModel itemsListModel = new DefaultListModel();
        for(Item item : story.getItems())
        {
            itemsListModel.addElement(item);
        }
        itemsList.setModel(itemsListModel);
    }

    private void loadStory(final String storyFile)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                Cursor previousCursor = mainPanel.getCursor();
                mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try
                {
                    progressBar.setVisible(true);
                    setAllData(OMManager.loadStoryFromXmlFile(storyFile, false).getStory());
                }
                catch (IFML2Exception e)
                {
                    LOG.error("Error while loading story!", e);
                    ifml2.GUIUtils.showErrorMessage(Editor.this, e);
                }
                finally
                {
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
                story = new Story();
                reloadDataInForm();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction(OPEN_STORY_ACTION_NAME, GUIUtils.OPEN_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
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
                catch (IFML2Exception e1)
                {
                    JOptionPane.showMessageDialog(Editor.this, "Ошибка во время сохранения истории: " + e1.getMessage());
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
                StoryOptionsEditor storyOptionsEditor = new StoryOptionsEditor(Editor.this, story.storyOptions, story.getLocations(), story.getProcedures());
                if(storyOptionsEditor.showDialog())
                {
                    storyOptionsEditor.getData(story.storyOptions);
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Используемые библиотеки...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UsedLibsEditor usedLibsEditor = new UsedLibsEditor(Editor.this);
                usedLibsEditor.setAllData(story.getLibraries()); //todo rewrite to ctor
                usedLibsEditor.setVisible(true);
            }
        });
        //storyMenu.add(new EditDictAction()); //https://www.hostedredmine.com/issues/11947
        storyMenu.add(new AbstractAction("Редактировать процедуры...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ProceduresEditor proceduresEditor = new ProceduresEditor(Editor.this, story.getProcedures());
                proceduresEditor.showDialog();
            }
        });
        storyMenu.add(new AbstractAction("Редактировать действия...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ActionsEditor actionsEditor = new ActionsEditor(Editor.this, story.getActions(), story.getProcedures(), story);
                actionsEditor.showDialog();
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Запустить историю в Плеере...", GUIUtils.PLAY_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String fileName;
                try
                {
                    File tempFile = File.createTempFile("ifml2run_", ".xml");
                    fileName = tempFile.getAbsolutePath();
                    saveStory(fileName);
                    GUIPlayer.startFromFile(fileName);
                    if (!tempFile.delete())
                    {
                        LOG.error(MessageFormat.format("Can't delete temp file {0}", tempFile.getAbsolutePath()));
                    }
                }
                catch (Throwable ex)
                {
                    JOptionPane.showMessageDialog(Editor.this,
                            MessageFormat.format("Ошибка во время сохранения истории во временный файл: {0}", Arrays.toString(ex.getStackTrace())));
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
        JFileChooser ifmlFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
        ifmlFileChooser.setFileFilter(new FileFilter()
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

        if(ifmlFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        return ifmlFileChooser.getSelectedFile().getAbsolutePath();
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
        if(location != null)
        {
            LocationEditor locationEditor = new LocationEditor(this, story, location);
            if(locationEditor.showDialog())
            {
                locationEditor.getData(location);
                return true;
            }
        }

        return false;
    }

    private String selectFileAndSaveStory() throws IFML2Exception
    {
        String storyFileName = selectFileForStorySave();
        if(storyFileName != null)
        {
            saveStory(storyFileName);
            return storyFileName;
        }

        return "";
    }

    private void saveStory(String storyFileName) throws IFML2Exception
    {
        OMManager.saveStoryToXmlFile(storyFileName, story);
    }

    private String selectFileForStorySave()
    {
        // choose story file:
        JFileChooser ifmlFileChooser = new JFileChooser(CommonUtils.getSamplesDirectory());
        ifmlFileChooser.setFileFilter(new FileFilter()
        {
            @Override
            public String getDescription()
            {
                return CommonConstants.STORY_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File file)
            {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.STORY_EXTENSION) || !file.exists();
            }
        });

        if(ifmlFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        String fileName = ifmlFileChooser.getSelectedFile().getAbsolutePath();

        if(!fileName.toLowerCase().endsWith(CommonConstants.STORY_EXTENSION))
        {
            fileName += CommonConstants.STORY_EXTENSION;
        }

        return fileName;
    }

    private boolean editItem(Item item)
    {
        if(item != null)
        {
            ItemEditor itemEditor = new ItemEditor(this, story, item);
            if(itemEditor.showDialog())
            {
                itemEditor.getData(item);
                return true;
            }
        }

        return false;
    }
}