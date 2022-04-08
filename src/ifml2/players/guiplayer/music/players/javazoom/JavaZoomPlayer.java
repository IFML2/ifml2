package ifml2.players.guiplayer.music.players.javazoom;

import ifml2.players.guiplayer.music.MusicManager;
import javazoom.jl.player.Player;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class JavaZoomPlayer implements MusicManager.MusicPlayer {
    Map<String, PlayerThread> playerThreadMap = new HashMap<>();

    @Override
    public void playMusic(String musicName, File musicFile) {
        // fixme обработка опции "Если эта музыка уже играет"
        PlayerThread playerThread = new PlayerThread(musicFile, thisPlayerThread -> playerThreadMap.remove(musicName));
        playerThreadMap.put(musicName.toLowerCase(Locale.ROOT), playerThread);
    }

    static class PlayerThread implements Runnable {
        private final Date created;
        Thread playThread;
        private final File file;
        private final Consumer<PlayerThread> consumer;
        Player player;

        public PlayerThread(File file, Consumer<PlayerThread> consumer) {
            this.file = file;
            this.consumer = consumer;
            this.created = new Date();
            playThread = new Thread(this, file.getAbsolutePath());
            playThread.start();
        }

        @Override
        public String toString() {
            return String.format("В %tT запущен %s", created, file.getName());
        }

        @Override
        public void run() {
            try {
                FileInputStream stream = new FileInputStream(file);
                player = new Player(stream);
                player.play();
                if (consumer != null)
                {
                    consumer.accept(this);
                }
            } catch (Exception ex) {
                //fixme обработка ошибок JOptionPane.showMessageDialog(mainPanel, ex.toString(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void stop() {
            player.close();
        }

        public File getFile() {
            return file;
        }
    }
}
