package ifml2.editor.gui.instructions;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.PlayMusicInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class PlayMusicInstrEditor extends AbstractInstrEditor {
    private static final String PLAY_MUSIC_EDITOR_TITLE = Instruction.getTitleFor(PlayMusicInstruction.class);
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<StoryOptions.Music> musicNameCombo;

    public PlayMusicInstrEditor(Window owner, PlayMusicInstruction instruction, @NotNull Story.DataHelper storyDataHelper){
        super(owner);
        initializeEditor(PLAY_MUSIC_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // load data
        EventList<StoryOptions.Music> musicList = storyDataHelper.getMusicList();
        musicNameCombo.setModel(new DefaultEventComboBoxModel<StoryOptions.Music>(musicList));
        Optional<StoryOptions.Music> optionalMusic = musicList.stream().filter(
                music -> music.getName().equalsIgnoreCase(instruction.getName())).findFirst();
        optionalMusic.ifPresent(music -> musicNameCombo.setSelectedItem(music));
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return PlayMusicInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        updateData(instruction);

        PlayMusicInstruction playMusicInstruction = (PlayMusicInstruction) instruction;
        StoryOptions.Music selectedMusic = (StoryOptions.Music) musicNameCombo.getSelectedItem();
        if (selectedMusic != null) {
            playMusicInstruction.setName(selectedMusic.getName());
        }
    }
}
