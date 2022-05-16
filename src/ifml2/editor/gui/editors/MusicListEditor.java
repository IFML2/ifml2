package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

import static ifml2.om.Word.Gender.FEMININE;

public class MusicListEditor extends AbstractEditor<List<StoryOptions.Music>> {
    private static final String MUSICLIST_EDITOR_TITLE = "Музыка";
    private final EventList<StoryOptions.Music> musicListClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private ListEditForm<StoryOptions.Music> musicListEditForm;

    public MusicListEditor(Window owner, @NotNull final EventList<StoryOptions.Music> musicList, Story.DataHelper storyDataHelper) {
        super(owner);
        initializeEditor(MUSICLIST_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        musicListClone = GlazedLists.eventList(musicList);

        musicListEditForm.bindData(musicListClone);
    }

    private void createUIComponents() {
        musicListEditForm = new ListEditForm<StoryOptions.Music>(this, "музыку", "музыки", FEMININE, StoryOptions.Music.class) {
            @Override
            protected StoryOptions.Music createElement() {
                StoryOptions.Music music = new StoryOptions.Music();
                return editMusic(music) ? music : null;
            }

            @Override
            protected boolean editElement(StoryOptions.Music selectedElement) {
                return editMusic(selectedElement);
            }

            private boolean editMusic(StoryOptions.@NotNull Music music){
                String fileName = music.getFileName();
                File previousFile = fileName != null ? new File(CommonUtils.getGamesDirectory(), fileName) : null;
                File file = GUIUtils.selectFile(MusicListEditor.this, CommonUtils.getGamesDirectory(),
                        CommonConstants.MUSIC_FILE_FILTER_NAME, CommonConstants.MP3_EXTENSION,
                        GUIUtils.MUSIC_FILE_ICON, previousFile);
                if (file == null) {
                    return false;
                }

                String name = music.getName();
                String musicName = GUIUtils.inputUniqueName(MusicListEditor.this, "Название музыки:", musicListClone,
                        StoryOptions.Music::getName, "Музыка с именем {0} уже есть.", name);

                if (musicName == null) {
                    return false;
                }

                music.setFileName(file.getName());
                music.setName(musicName);

                return true;
            }
        };
    }

    @Override
    public void updateData(@NotNull List<StoryOptions.Music> musicList) throws IFML2EditorException {
        musicList.clear();
        musicList.addAll(musicListClone);
    }
}
