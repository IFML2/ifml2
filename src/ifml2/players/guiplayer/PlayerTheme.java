package ifml2.players.guiplayer;

import java.awt.*;

public class PlayerTheme {
    private String name;
    private Color fontColor;
    private Color backgroundColor;
    private String fontName;
    private int fontSize;

    PlayerTheme(String name, Color fontColor, Color backgroundColor, String fontName, int fontSize) {
        this.name = name;
        this.fontColor = fontColor;
        this.backgroundColor = backgroundColor;
        this.fontName = fontName;
        this.fontSize = fontSize;
    }

    @Override
    public String toString() {
        return name;
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
}
