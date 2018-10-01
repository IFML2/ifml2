package ifml2.players.guiplayer;

import java.util.prefs.Preferences;

class PlayerPreferences {
    private static final String PLAYER_PREFS_ROOT = "/ifml2/player";
    private static final String THEME_KEY = "themename";
    private static Preferences _preferences = Preferences.userRoot().node(PLAYER_PREFS_ROOT);

    static String getPlayerThemeName() {
        return _preferences.get(THEME_KEY, PlayerTheme.DEFAULT_THEME.getName());
    }

    static void setPlayerThemeName(String playerThemeName) {
        _preferences.put(THEME_KEY, playerThemeName);
    }
}
