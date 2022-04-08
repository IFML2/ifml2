package ifml2.players.guiplayer.music;

import ifml2.players.guiplayer.music.players.javazoom.JavaZoomPlayer;

import java.io.File;

public class MusicManager {
    private final MusicPlayer musicPlayer;

    public MusicManager(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public void StartPlay(String musicName, File musicFile){
        musicPlayer.playMusic(musicName, musicFile);
    }

    public interface MusicPlayer {
        void playMusic(String musicName, File musicFile);
    }

    public static MusicPlayer CreateJavaZoomPlayer() {
        return new JavaZoomPlayer();
    }
}