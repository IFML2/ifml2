package ifml2.players.guiplayer.music;

import ifml2.players.guiplayer.music.players.javazoom.JavaZoomPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface IMusicPlayer {
    void playMusic(String musicName, File musicFile, boolean isInfinite);

    void stopMusic(String musicName);

    void stopAll();

    static @NotNull IMusicPlayer CreateDefaultPlayer() {
        return new JavaZoomPlayer();
    }
}
