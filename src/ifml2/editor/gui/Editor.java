package ifml2.editor.gui;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.engine.EngineVersion;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.OMManager;
import ifml2.om.Story;
import ifml2.players.guiplayer.GUIPlayer;
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
    private final JMenuBar mainMenu = new JMenuBar();
    private final JPopupMenu locationPopupMenu = new JPopupMenu();

    private final JFrame frame;

    private Story story = new Story();

    private final NewLocationAction newLocationAction = new NewLocationAction();
    private final EditLocationAction editLocationAction = new EditLocationAction();
    private final DelLocationAction delLocationAction = new DelLocationAction();

    private final EditItemAction editItemAction = new EditItemAction();
    private final DelItemAction delItemAction = new DelItemAction();

    private static final String FILE_MENU_NAME = "Файл";
    private static final String STORY_MENU_NAME = "История";
    private static final String OPEN_STORY_ACTION_NAME = "Открыть...";
    private static final String EDIT_LOCATION_ACTION_NAME = "Редактировать...";
    private static final Logger LOG = Logger.getLogger(Editor.class);

    public Editor()
    {
        frame = this;

        String IFML_EDITOR_VERSION = "ЯРИЛ 2.0 (" + EngineVersion.IFML_ENGINE_VERSION + ") Редактор";
        setTitle(IFML_EDITOR_VERSION);
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        createMainMenu();
        setJMenuBar(mainMenu);

        createPopupMenus();

        GUIUtils.packAndCenterWindow(frame);

        newLocButton.setAction(newLocationAction);
        editLocButton.setAction(editLocationAction);
        delLocButton.setAction(delLocationAction);

        NewItemAction newItemAction = new NewItemAction();
        newItemButton.setAction(newItemAction);
        editItemButton.setAction(editItemAction);
        delItemButton.setAction(delItemAction);

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
                try
                {
                    mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    progressBar.setVisible(true);
                    setAllData(OMManager.loadStoryFromXmlFile(storyFile, false).getStory());
                }
                catch (IFML2Exception e)
                {
                    LOG.error("Error while loading story!", e);
                    ifml2.GUIUtils.showErrorMessage(frame, e);
                }
                finally
                {
                    progressBar.setVisible(false);
                    mainPanel.setCursor(previousCursor);
                }
            }
        }.start();
    }

    private void createMainMenu()
    {
        JMenu fileMenu = new JMenu(FILE_MENU_NAME);
        fileMenu.add(new NewStoryAction());
        fileMenu.addSeparator();
        fileMenu.add(new OpenStoryAction());
        fileMenu.add(new SaveStoryAction());
        mainMenu.add(fileMenu);

        JMenu storyMenu = new JMenu(STORY_MENU_NAME);
        storyMenu.add(new EditStoryOptions());
        storyMenu.addSeparator();
        storyMenu.add(new EditUsedLibsAction());
        //storyMenu.add(new EditDictAction()); //https://www.hostedredmine.com/issues/11947
        storyMenu.add(new EditProceduresAction());
        storyMenu.add(new EditActionsAction());
        storyMenu.addSeparator();
        storyMenu.add(new RunStoryAction());
        mainMenu.add(storyMenu);
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

        if(ifmlFileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        return ifmlFileChooser.getSelectedFile().getAbsolutePath();
    }

    private void createPopupMenus()
    {
        // locations popup
        locationPopupMenu.add(newLocationAction);
        locationPopupMenu.addSeparator();
        locationPopupMenu.add(editLocationAction);
        locationPopupMenu.add(delLocationAction);

        /*locationPopupMenu.addPopupMenuListener(new PopupMenuListener()
        {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                Object selectedLoc = locationsList.getSelectedValue();
                editLocationAction.setEnabled(selectedLoc != null);
                delLocationAction.setEnabled(selectedLoc != null);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}

            @Override
            public void popupMenuCanceled(PopupMenuEvent e){}
        });*/
    }

    private class OpenStoryAction extends AbstractAction
    {
        private OpenStoryAction()
        {
            super(OPEN_STORY_ACTION_NAME);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            String storyFileName = selectStoryFileForOpen();
            if(storyFileName != null)
            {
                loadStory(storyFileName);
            }
        }
    }

    private class EditLocationAction extends AbstractAction
    {
        private EditLocationAction()
        {
            super(EDIT_LOCATION_ACTION_NAME);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            editLocation((Location) locationsList.getSelectedValue());
        }
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
        DictionaryEditor dictionaryEditor = new DictionaryEditor();
        dictionaryEditor.setAllData(story.dictionary);
        dictionaryEditor.setVisible(true);
    }*/


    private boolean editLocation(Location location)
    {
        if(location != null)
        {
            LocationEditor locationEditor = new LocationEditor();

            locationEditor.setAllData(story, location);
            locationEditor.setVisible(true);
            if(locationEditor.isOk)
            {
                locationEditor.getAllData(location);
                return true;
            }
        }

        return false;
    }

    private class SaveStoryAction extends AbstractAction
    {
        private SaveStoryAction()
        {
            super("Сохранить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                selectFileAndSaveStory();
            }
            catch (IFML2Exception e1)
            {
                JOptionPane.showMessageDialog(frame, "Ошибка во время сохранения истории: " + e1.getMessage());
            }
        }
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

        if(ifmlFileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
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

    private class NewStoryAction extends AbstractAction
    {
        private NewStoryAction()
        {
            super("Новая история");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            story = new Story();
            reloadDataInForm();
        }
    }

    private class EditUsedLibsAction extends AbstractAction
    {
        public EditUsedLibsAction()
        {
            super("Используемые библиотеки...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            UsedLibsEditor usedLibsEditor = new UsedLibsEditor();
            usedLibsEditor.setAllData(story.libraries);
            usedLibsEditor.setVisible(true);
        }
    }

    private class NewLocationAction extends AbstractAction
    {
        private NewLocationAction()
        {
            super("Добавить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Location location = new Location();
            if(editLocation(location))
            {
                story.getLocations().add(location);
                reloadDataInForm();
                locationsList.setSelectedValue(location, true);
            }
        }
    }

    private class DelLocationAction extends AbstractAction
    {
        private DelLocationAction()
        {
            super("Удалить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Location location = (Location) locationsList.getSelectedValue();
            if(location != null)
            {
                int answer = JOptionPane.showConfirmDialog(frame, "Вы уверены, что хотите удалить эту локацию?",
                        "Удаление локации", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(JOptionPane.YES_OPTION == answer)
                {
                    story.getLocations().remove(location);
                    reloadDataInForm();
                }
            }
        }
    }

    private class RunStoryAction extends AbstractAction
    {
        private RunStoryAction()
        {
            super("Запустить историю в плеере...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            JOptionPane.showMessageDialog(frame, "Сначала нужно сохранить историю", "Сохранение перед запуском",
                    JOptionPane.INFORMATION_MESSAGE);
            String fileName = null;
            try
            {
                fileName = selectFileAndSaveStory();
            }
            catch (IFML2Exception e1)
            {
                JOptionPane.showMessageDialog(frame, "Ошибка во время сохранения истории: " + Arrays.toString(e1.getStackTrace()));
            }
            GUIPlayer.main(new String[]{fileName});
        }
    }

    private class EditStoryOptions extends AbstractAction
    {
        private EditStoryOptions()
        {
            super("Настройки истории...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            StoryOptionsEditor storyOptionsEditor = new StoryOptionsEditor();

            storyOptionsEditor.setAllData(story.storyOptions, story.getLocations(), story.getProcedures());
            storyOptionsEditor.setVisible(true);

            if(storyOptionsEditor.isOk)
            {
                storyOptionsEditor.getAllData(story.storyOptions);
            }
        }
    }

    private class EditProceduresAction extends AbstractAction
    {
        private EditProceduresAction()
        {
            super("Редактировать процедуры...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ProceduresEditor proceduresEditor = new ProceduresEditor();
            proceduresEditor.setAllData(story.getProcedures());
            proceduresEditor.setVisible(true);
        }
    }


    private class EditActionsAction extends AbstractAction
    {
        private EditActionsAction()
        {
            super("Редактировать действия...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            ActionsEditor actionsEditor = new ActionsEditor();
            actionsEditor.setAllData(story.getActions());
            actionsEditor.setVisible(true);
        }
    }

    private class NewItemAction extends AbstractAction
    {
        private NewItemAction()
        {
            super("Добавить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = new Item();
            if(editItem(item))
            {
                story.getItems().add(item);
                reloadDataInForm();
                itemsList.setSelectedValue(item, true);
            }
        }
    }

    private boolean editItem(Item item)
    {
        if(item != null)
        {
            ItemEditor itemEditor = new ItemEditor(story, item);

            itemEditor.setVisible(true);
            if(itemEditor.isOk)
            {
                itemEditor.getData(item);
                return true;
            }
        }

        return false;
    }

    private class EditItemAction extends AbstractAction
    {
        private EditItemAction()
        {
            super("Редактировать...");
        }

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
    }

    private class DelItemAction extends AbstractAction
    {
        private DelItemAction()
        {
            super("Удалить...");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Item item = (Item) itemsList.getSelectedValue();
            if(item != null)
            {
                int answer = JOptionPane.showConfirmDialog(frame, "Вы уверены, что хотите удалить этот предмет?");
                if(answer == 0)
                {
                    story.getItems().remove(item);
                    reloadDataInForm();
                }
            }
        }
    }
}