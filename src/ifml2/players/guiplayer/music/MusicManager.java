package ifml2.players.guiplayer.music;

import ifml2.players.guiplayer.music.players.javazoom.JavaZoomPlayer;

import java.io.File;

public class MusicManager {
    private final MusicPlayer musicPlayer;

    public MusicManager(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public void startPlay(String musicName, File musicFile, boolean isInfinite){
        musicPlayer.playMusic(musicName, musicFile, isInfinite);
    }

    public void stopPlay(String musicName) {
        musicPlayer.stopMusic(musicName);
    }

    public void stopAll() {
        musicPlayer.stopAll();
    }

    public interface MusicPlayer {
        void playMusic(String musicName, File musicFile, boolean isInfinite);

        void stopMusic(String musicName);

        void stopAll();
    }

    public static MusicPlayer CreateJavaZoomPlayer() {
        return new JavaZoomPlayer();
    }
}