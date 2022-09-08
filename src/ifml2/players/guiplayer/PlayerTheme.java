package ifml2.players.guiplayer;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerTheme {
    private static final PlayerTheme LIGHT_THEME = new PlayerTheme("Светлая", new Color(0x000000), new Color(0xF0F0F0), Font.SANS_SERIF, 18);
    private static final PlayerTheme DARK_THEME = new PlayerTheme("Тёмная", new Color(0xC0C0C0), new Color(0x303030), Font.SERIF, 18);
    private static final PlayerTheme CONTRAST_THEME = new PlayerTheme("Контрастная", new Color(0x00FF00), new Color(0x120021), Font.SANS_SERIF, 20);
    static Map<String, PlayerTheme> DEFAULT_PLAYER_THEMES = new LinkedHashMap<String, PlayerTheme>() {
        {
            put(LIGHT_THEME);
            put(DARK_THEME);
            put(CONTRAST_THEME);
        }

        private void put(@NotNull PlayerTheme playerTheme) {
            put(playerTheme.getName(), playerTheme);
        }
    };// Arrays.asList(LIGHT_THEME, DARK_THEME, CONTRAST_THEME);
    static PlayerTheme DEFAULT_THEME = DARK_THEME;
    private String name;
    private Color fontColor;
    private Color backgroundColor;
    private String fontName;
    private int fontSize;

    private PlayerTheme(String name, Color fontColor, Color backgroundColor, String fontName, int fontSize) {
        this.name = name;
        this.fontColor = fontColor;
        this.backgroundColor = backgroundColor;
        this.fontName = fontName;
        this.fontSize = fontSize;
    }

    @Override
    public String toString() {
        return getName();
    }

    Color getFontColor() {
        return fontColor;
    }

    Color getBackgroundColor() {
        return backgroundColor;
    }

    String getFontName() {
        return fontName;
    }

    int getFontSize() {
        return fontSize;
    }

    public String getName() {
        return name;
    }
}
