package ifml2.editor.gui.editors;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
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
            protected StoryOptions.Music createElement() throws Exception {
                return null; //fixme
            }

            @Override
            protected boolean editElement(StoryOptions.Music selectedElement) throws Exception {
                return super.editElement(selectedElement); //fixme
            }
        };
    }

    @Override
    public void updateData(@NotNull List<StoryOptions.Music> musicList) throws IFML2EditorException {
        musicList.clear();
        musicList.addAll(musicListClone);
    }
}
