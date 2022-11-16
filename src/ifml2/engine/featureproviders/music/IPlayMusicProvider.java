package ifml2.engine.featureproviders.music;

import ifml2.engine.featureproviders.IPlayerFeatureProvider;

import java.io.File;

/**
 * Play music provider.
 */
public interface IPlayMusicProvider extends IPlayerFeatureProvider {
    /**
     * Starts playing music file with assigned name
     *
     * @param musicName  name (id) of music
     * @param musicFile  music file
     * @param isInfinite to play music in infinite loop
     */
    void startMusic(String musicName, File musicFile, boolean isInfinite);

    void stopMusic(String musicName);
}
