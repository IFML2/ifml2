package ifml2.players.guiplayer.music.players.javazoom;

import ifml2.FormatLogger;
import ifml2.players.guiplayer.music.IMusicPlayer;
import javazoom.jl.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class JavaZoomPlayer implements IMusicPlayer {
    Map<String, PlayerThread> playerThreadMap = new HashMap<>();

    @Override
    public void playMusic(@NotNull String musicName, @NotNull File musicFile, boolean isInfinite) {
        // fixme обработка опции "Если эта музыка уже играет"
        if (!musicFile.exists())
            return;
        LOG.debug("Starting PlayerThread with music file {0}, {1}.", musicFile, isInfinite ? "infinite" : "once");
        PlayerThread playerThread = new PlayerThread(
            musicFile,
            isInfinite,
            thisPlayerThread -> {
                playerThreadMap.remove(musicName, thisPlayerThread);
                LOG.debug("{0} music file(s) are playing now.", playerThreadMap.size());
            }
        );
        playerThreadMap.put(musicName.toLowerCase(Locale.ROOT), playerThread);
        LOG.debug("{0} music file(s) are playing now.", playerThreadMap.size());
    }

    @Override
    public void stopMusic(@NotNull String musicName) {
        PlayerThread playerThread = playerThreadMap.get(musicName.toLowerCase(Locale.ROOT));
        if (playerThread != null){
            LOG.debug("Stopping PlayerThread with music file {0}.", playerThread.getFile());
            playerThread.stop();
        }
    }

    @Override
    public void stopAll() {
        LOG.debug("Stopping all PlayerThreads.");
        for (PlayerThread playerThread : playerThreadMap.values()) {
            playerThread.stop();
        }
        playerThreadMap.clear();
    }

    static class PlayerThread implements Runnable {
        private final Date created;
        Thread playThread;
        private final File file;
        private volatile boolean isInfinitePlay;
        private final Consumer<PlayerThread> finalizationConsumer;
        Player player;

        public PlayerThread(@NotNull File file, boolean isInfinitePlay, Consumer<PlayerThread> finalizationConsumer) {
            this.file = file;
            this.isInfinitePlay = isInfinitePlay;
            this.finalizationConsumer = finalizationConsumer;
            this.created = new Date();
            playThread = new Thread(this, file.getName());
            playThread.start();
        }

        @Override
        public String toString() {
            return String.format("В %tT запущен %s", created, file.getName());
        }

        @Override
        public void run() {
            try {
                do {
                    try (FileInputStream stream = new FileInputStream(file)) {
                        player = new Player(stream);
                        LOG.debug("Starting playing music from file {0}.", file);
                        player.play();
                        LOG.debug("Playing music from file {0} ends.", file);
                    }
                } while (isInfinitePlay);

                if (finalizationConsumer != null)
                {
                    finalizationConsumer.accept(this);
                }
            } catch (Exception ex) {
                LOG.error("Error while playing music: {0}", ex);
            }
        }

        public void stop() {
            isInfinitePlay = false;
            LOG.debug("Closing player for music file {0}.", file);
            player.close();
        }

        public File getFile() {
            return file;
        }

        private static final FormatLogger LOG = FormatLogger.getLogger(PlayerThread.class);
    }

    private static final FormatLogger LOG = FormatLogger.getLogger(JavaZoomPlayer.class);
}
