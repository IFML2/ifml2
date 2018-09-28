package ifml2.players.guiplayer;

import java.util.prefs.Preferences;

class PlayerPreferences {
    private static final String PLAYER_PREFS_ROOT = "/IFML2/Player";
    private static final String THEME_KEY = "ThemeName";
    private static Preferences _preferences = Preferences.userRoot().node(PLAYER_PREFS_ROOT);

    static String getPlayerThemeName() {
        return _preferences.get(THEME_KEY, PlayerTheme.DEFAULT_THEME.getName());
    }

    static void setPlayerThemeName(String playerThemeName) {
        _preferences.put(THEME_KEY, playerThemeName);
    }
}
