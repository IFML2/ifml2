package ifml2;

import ifml2.om.Story;

public interface Environment {

    Story getStory();

    void setStory(Story story);

    void outText(String text);

    void outText(String text, Object... args);

    void outIcon(String iconFilePath, int maxHeight, int maxWidth);

    boolean isDebug();

    void debugOn();

    void debugOff();

    void debugToggle();

    void debug(String text);

    void debug(String text, Object... args);

}
