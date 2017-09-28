package ifml2.engine;

import ifml2.IFML2Exception;
import ifml2.om.Location;
import ifml2.om.Story;
import ifml2.vm.IFML2VMException;
import ifml2.vm.Variable;
import ifml2.vm.values.Value;

public interface Engine {

    Story getStory();

    void setCurrentLocation(Location currentLocation);

    void outText(String text, Object... args);

    void outTextLn(String text, Object... args);

    void outIcon(String iconFilePath, int maxHeight, int maxWidth);

    Value resolveSymbol(String symbol) throws IFML2VMException;

    Variable searchGlobalVariable(String name);

    boolean executeGamerCommand(String gamerCommand);

    void loadGame(String saveFileName) throws IFML2Exception;

    void saveGame(String saveFileName) throws IFML2Exception;

    void loadStory(String storyFileName, boolean isAllowedOpenCipherFiles) throws IFML2Exception;

    void initGame() throws IFML2Exception;
}
