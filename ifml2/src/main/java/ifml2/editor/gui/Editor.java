package ifml2.editor.gui;

import ca.odell.glazedlists.EventList;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.editors.ActionEditor;
import ifml2.editor.gui.editors.InheritedSystemProceduresEditor;
import ifml2.editor.gui.editors.ItemEditor;
import ifml2.editor.gui.editors.LocationEditor;
import ifml2.editor.gui.editors.ProcedureEditor;
import ifml2.editor.gui.editors.StoryOptionsEditor;
import ifml2.editor.gui.editors.UsedLibsEditor;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.engine.EngineVersion;
import ifml2.om.Action;
import ifml2.om.InheritedSystemProcedures;
import ifml2.om.Item;
import ifml2.om.Library;
import ifml2.om.Location;
import ifml2.om.OMManager;
import ifml2.om.Procedure;
import ifml2.om.Story;
import ifml2.om.Word;
import ifml2.players.guiplayer.GUIPlayer;
import ifml2.tests.gui.TestRunner;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

import static ifml2.CommonConstants.RUSSIAN_PRODUCT_NAME;
import static ifml2.GUIUtils.DIRECTORY_ICON;
import static ifml2.GUIUtils.STORY_FILE_ICON;
import static java.lang.String.format;

public class Editor extends JFrame {
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
    private File storyFile = null; // TODO: 07.02.2016 перейти везде на File, а не на String

    public Editor() {
        updateTitle();
        setContentPane(mainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (ensureStorySaved()) {
                    dispose();
                }
            }
        });

        setJMenuBar(createMainMenu());

        GUIUtils.packAndCenterWindow(this);

        // create new story at start
        createNewStory();
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            Editor editor = new Editor();
            editor.setVisible(true);

            if (args != null && args.length >= 1) {
                // load story
                editor.loadStory(args[0]);
            }
        });
    }

    /**
     * Создаём новую историю, устанавливаем её, обнуляем файл истории.
     */
    private void createNewStory() {
        Story story = new Story();
        setStory(story, null);
    }

    private boolean editProcedure(@Nullable Procedure procedure) {
        if (procedure != null) {
            try {
                ProcedureEditor procedureEditor = new ProcedureEditor(this, procedure, story.getDataHelper());
                if (procedureEditor.showDialog()) {
                    procedureEditor.updateData(procedure);
                    return true;
                }
            } catch (IFML2EditorException e) {
                GUIUtils.showErrorMessage(this, e);
            }
        }

        return false;
    }

    private boolean editAction(@Nullable Action action) throws IFML2EditorException {
        if (action != null) {
            ActionEditor actionEditor = new ActionEditor(this, action, story.getDataHelper());
            if (actionEditor.showDialog()) {
                actionEditor.updateData(action);
                return true;
            }
        }

        return false;
    }

    /**
     * Устанавливает историю, сбрасывает флаг правки истории, пересвязывает данные в списках.
     *
     * @param story    история
     * @param filePath путь к файлу истории
     */
    public void setStory(@NotNull Story story, String filePath) {
        this.story = story;
        this.storyFile = filePath != null ? new File(filePath) : null;

        setStoryEdited(false); // reset edited flag
        bindData();
    }

    public void setStoryEdited(boolean storyEdited) {
        isStoryEdited = storyEdited;
        updateTitle();
    }

    private void markStoryEdited() {
        setStoryEdited(true);
    }

    /**
     * Updates Editor's title - including story filename and modification asterisk (*)
     */
    private void updateTitle() {
        String fileName = storyFile != null ? storyFile.getName() : "новая история";
        String storyEditedMark = isStoryEdited ? " - * история не сохранена" : "";
        String editorTitle = format("%s Редактор %s -- %s%s", RUSSIAN_PRODUCT_NAME, EngineVersion.VERSION, fileName,
                storyEditedMark);
        setTitle(editorTitle);
    }

    private int askAboutSavingStory() {
        return JOptionPane.showConfirmDialog(Editor.this, "Вы хотите сохранить историю?", "История не сохранена",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    private void bindData() {
        locationsListEditForm.bindData(story.getLocations());
        itemsListEditForm.bindData(story.getItems());
        proceduresListEditForm.bindData(story.getProcedures());
        actionsListEditForm.bindData(story.getActions());
    }

    private void loadStory(final String storyFilePath) {
        new Thread() // todo remake to SwingWorker
        {
            @Override
            public void run() { //todo rewrite using SwingWorker
                Cursor previousCursor = mainPanel.getCursor();
                mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                //getContentPane().setEnabled(false);
                try {
                    progressBar.setVisible(true);
                    Story story = OMManager.loadStoryFromFile(storyFilePath, false, false).getStory();
                    Editor.this.setStory(story, storyFilePath);
                } catch (Throwable e) {
                    LOG.error("Error while loading story!", e);
                    GUIUtils.ReportError(Editor.this, e);
                    //GUIUtils.showErrorMessage(Editor.this, e);
                } finally {
                    //getContentPane().setEnabled(true); // todo try SwingUtilities.invokeLater
                    progressBar.setVisible(false);
                    mainPanel.setCursor(previousCursor);
                }
            }
        }.start();
    }

    private JMenuBar createMainMenu() {
        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu(FILE_MENU_NAME);
        fileMenu.add(new AbstractAction("Новая история", GUIUtils.NEW_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ensureStorySaved()) {
                    return;
                }

                createNewStory();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction(OPEN_STORY_ACTION_NAME, GUIUtils.OPEN_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ensureStorySaved()) {
                    return;
                }

                String storyFileName = selectStoryFileForOpen();
                if (storyFileName != null) {
                    loadStory(storyFileName);
                }
            }
        });
        fileMenu.add(new AbstractAction("Сохранить...", GUIUtils.SAVE_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    selectFileAndSaveStory();
                } catch (IFML2Exception ex) {
                    JOptionPane.showMessageDialog(Editor.this, "Ошибка во время сохранения истории: " + ex.getMessage());
                    GUIUtils.ReportError(Editor.this, ex);
                }
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Экспортировать зашифрованную историю...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // choose cipher story file:
                JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getGamesDirectory());
                storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
                storyFileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return CommonConstants.CIPHERED_STORY_FILE_FILTER_NAME;
                    }

                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.CIPHERED_STORY_EXTENSION) ||
                                !file.exists();
                    }
                });

                storyFileChooser.setFileView(new FileView() {
                    @Override
                    public Icon getIcon(File f) {
                        if (f.isDirectory()) {
                            return DIRECTORY_ICON;
                        }
                        return GUIUtils.CIPHERED_STORY_FILE_ICON;
                    }
                });

                if (storyFileChooser.showSaveDialog(Editor.this) == JFileChooser.APPROVE_OPTION) {
                    String fileName = storyFileChooser.getSelectedFile().getAbsolutePath();

                    if (!fileName.toLowerCase().endsWith(CommonConstants.CIPHERED_STORY_EXTENSION)) {
                        fileName += CommonConstants.CIPHERED_STORY_EXTENSION;
                    }

                    try {
                        OMManager.exportCipheredStory(fileName, story);
                    } catch (IFML2Exception ex) {
                        GUIUtils.ReportError(Editor.this, ex);
                    }
                }
            }
        });
        mainMenu.add(fileMenu);

        JMenu storyMenu = new JMenu(STORY_MENU_NAME);
        storyMenu.add(new AbstractAction("Настройки истории...", GUIUtils.PREFERENCES_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                StoryOptionsEditor storyOptionsEditor = new StoryOptionsEditor(Editor.this, story.getStoryOptions(), story.getDataHelper());
                if (storyOptionsEditor.showDialog()) {
                    storyOptionsEditor.updateData(story.getStoryOptions());
                    markStoryEdited();
                }
            }
        });
        storyMenu.add(new AbstractAction("Перехваты системных процедур...", GUIUtils.PREFERENCES_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                InheritedSystemProcedures inheritedSystemProcedures = story.getInheritedSystemProcedures();
                InheritedSystemProceduresEditor inheritedSystemProceduresEditor = new InheritedSystemProceduresEditor(Editor.this,
                        inheritedSystemProcedures, story.getDataHelper());
                if (inheritedSystemProceduresEditor.showDialog()) {
                    try {
                        inheritedSystemProceduresEditor.updateData(inheritedSystemProcedures);
                        markStoryEdited();
                    } catch (IFML2EditorException ex) {
                        GUIUtils.ReportError(Editor.this, ex);
                    }
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Используемые библиотеки...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventList<Library> libraries = story.getLibraries();
                UsedLibsEditor usedLibsEditor = new UsedLibsEditor(Editor.this, libraries, story.getDataHelper());
                if (usedLibsEditor.showDialog()) {
                    try {
                        usedLibsEditor.updateData(libraries);
                        markStoryEdited();
                    } catch (Throwable ex) {
                        GUIUtils.ReportError(Editor.this, ex);
                    }
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Запустить историю в Плеере...", GUIUtils.PLAY_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (storyFile == null) {
                        JOptionPane.showMessageDialog(Editor.this, "Прежде, чем запускать в Плеере, необходимо сохранить историю хотя бы один раз.",
                                "Необходимо сохранить историю", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    File storyFolder = storyFile.getParentFile();
                    File tempFile = File.createTempFile("ifml2run_", ".temp", storyFolder);
                    tempFile.deleteOnExit();
                    String fileName = tempFile.getAbsolutePath();
                    saveStory(fileName, false);
                    SwingUtilities.invokeLater(() -> GUIPlayer.startFromFile(fileName, true));
                } catch (Throwable ex) {
                    GUIUtils.ReportError(Editor.this, ex);
                }
            }
        });
        storyMenu.addSeparator();
        storyMenu.add(new AbstractAction("Открыть Тестер...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestRunner.main(new String[]{});
            }
        });
        mainMenu.add(storyMenu);

        return mainMenu;
    }

    private boolean ensureStorySaved() {
        boolean isOk = true;
        if (isStoryEdited) {
            int answer = askAboutSavingStory();
            switch (answer) {
                case JOptionPane.YES_OPTION:
                    try {
                        if (!selectFileAndSaveStory()) {
                            isOk = false;
                        }
                    } catch (IFML2Exception ex) {
                        GUIUtils.showErrorMessage(Editor.this, ex);
                        isOk = false;
                    }
                    break;
                case JOptionPane.CANCEL_OPTION:
                    isOk = false;
                    break;
            }
        }
        return isOk;
    }

    private String selectStoryFileForOpen() {
        // choose story file:
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getGamesDirectory());
        storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
        storyFileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return CommonConstants.STORY_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.STORY_EXTENSION);
            }
        });

        storyFileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                return f.isDirectory() ? DIRECTORY_ICON : STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return storyFileChooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    private boolean editLocation(@Nullable Location location) {
        if (location != null) {
            LocationEditor locationEditor = new LocationEditor(this, location, story.getDataHelper());
            if (locationEditor.showDialog()) {
                locationEditor.updateData(location);
                return true;
            }
        }

        return false;
    }

    private boolean selectFileAndSaveStory() throws IFML2Exception {
        String storyFileName = selectFileForStorySave();
        if (storyFileName != null) {
            saveStory(storyFileName, true);
            setStoryFile(storyFileName);
            return true;
        }

        return false;
    }

    private void saveStory(String storyFileName, boolean toMarkAsSaved) throws IFML2Exception {
        OMManager.saveStoryToXmlFile(storyFileName, story);
        if (toMarkAsSaved) {
            setStoryEdited(false); // reset edited flag
        }
    }

    private String selectFileForStorySave() {
        // choose story file:
        JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getGamesDirectory());
        storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
        storyFileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return CommonConstants.STORY_FILE_FILTER_NAME;
            }

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.STORY_EXTENSION) ||
                        !file.exists();
            }
        });

        storyFileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                return f.isDirectory() ? DIRECTORY_ICON : STORY_FILE_ICON;
            }
        });

        if (storyFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        String fileName = storyFileChooser.getSelectedFile().getAbsolutePath();

        if (!fileName.toLowerCase().endsWith(CommonConstants.STORY_EXTENSION)) {
            fileName += CommonConstants.STORY_EXTENSION;
        }

        return fileName;
    }

    private boolean editItem(@Nullable Item item) {
        if (item != null) {
            ItemEditor itemEditor = new ItemEditor(this, item, story.getDataHelper());
            if (itemEditor.showDialog()) {
                itemEditor.updateData(item);
                return true;
            }
        }

        return false;
    }

    public void setStoryFile(String storyFileName) {
        this.storyFile = new File(storyFileName);
        updateTitle();
    }

    private void createUIComponents() {
        locationsListEditForm = new ListEditForm<Location>(this, "локацию", "локации", Word.Gender.FEMININE, Location.class) {
            {
                addListChangeListener(e -> markStoryEdited());
            }

            @Override
            protected void addElementToList(Location location) {
                if (location != null) {
                    story.addLocation(location);
                }
            }

            @Override
            protected Location createElement() throws Exception {
                Location location = new Location();
                return editLocation(location) ? location : null;
            }

            @Override
            protected boolean editElement(Location selectedElement) {
                return editLocation(selectedElement);
            }


            //todo warn that location will be deleted from items
        };

        itemsListEditForm = new ListEditForm<Item>(this, "предмет", "предмета", Word.Gender.MASCULINE, Item.class) {
            {
                addListChangeListener(e -> markStoryEdited());
            }

            @Override
            protected void addElementToList(Item item) {
                if (item != null) {
                    story.addItem(item);
                }
            }

            @Override
            protected Item createElement() throws Exception {
                Item item = new Item();
                return editItem(item) ? item : null;
            }

            @Override
            protected boolean editElement(Item selectedElement) {
                return editItem(selectedElement);
            }


        };

        proceduresListEditForm = new ListEditForm<Procedure>(this, "процедуру", "процедуры", Word.Gender.FEMININE, Procedure.class) {
            {
                addListChangeListener(e -> markStoryEdited());
            }

            @Override
            protected Procedure createElement() throws Exception {
                Procedure procedure = new Procedure();
                return editProcedure(procedure) ? procedure : null;
            }

            @Override
            protected boolean editElement(Procedure selectedElement) {
                return editProcedure(selectedElement);
            }

            @Override
            protected boolean beforeDelete(Procedure selectedElement) throws Exception {
                if (selectedElement != null) {
                    // search for usages in actions
                    ArrayList<Action> affectedActionsList = story.getDataHelper().findActionsByProcedure(selectedElement);
                    if (affectedActionsList.size() > 0) {
                        String message = MessageFormat
                                .format("Это процедура вызывается в действиях:\n{0}\n" + "Поэтому она не может быть удалена.",
                                        affectedActionsList.toString());
                        JOptionPane.showMessageDialog(Editor.this, message, "Процедура используется", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    // check for usage as started procedure
                    if (selectedElement.equals(story.getStoryOptions().getStartProcedureOption().getProcedure())) {
                        int answer = JOptionPane.showConfirmDialog(Editor.this,
                                "Эта процедура установлена как стартовая. Всё равно удалить?\n" +
                                        "В этом случае стартовая процедура будет сброшена.", "Процедура используется", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        return answer == JOptionPane.YES_OPTION;
                    }

                    // check for usages as inherited system procedures
                    if (selectedElement.equals(story.getInheritedSystemProcedures().getParseErrorHandler())) {
                        int answer = JOptionPane.showConfirmDialog(Editor.this,
                                "Эта процедура установлена как перекрывающая системный обработчик ошибки парсинга. Всё равно удалить?\n" +
                                        "В этом случае перекрытие будет отменено.", "Процедура используется", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        return answer == JOptionPane.YES_OPTION;
                    }

                    return super.beforeDelete(selectedElement); // do standard asking
                }

                return false;
            }
        };

        actionsListEditForm = new ListEditForm<Action>(this, "действие", "действия", Word.Gender.NEUTER, Action.class) {
            {
                addListChangeListener(e -> markStoryEdited());
            }

            @Override
            protected Action createElement() throws Exception {
                Action action = new Action();
                return editAction(action) ? action : null;
            }

            @Override
            protected boolean editElement(Action selectedElement) throws Exception {
                return editAction(selectedElement);
            }
        };
    }
}