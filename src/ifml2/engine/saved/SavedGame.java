package ifml2.engine.saved;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.Location;
import ifml2.om.Story;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "saved-game")
public class SavedGame
{
    @XmlElementWrapper(name = "global-vars")
    @XmlElement(name = "var")
    public List<SavedVariable> globalVars;
    @XmlElementWrapper(name = "system-vars")
    @XmlElement(name = "var")
    public List<SavedVariable> systemVars;

    @XmlElementWrapper(name = "inventory")
    @XmlElement(name = "item")
    public List<String> inventory;

    @XmlElementWrapper(name = "locations-items")
    @XmlElement(name = "loc")
    public List<SavedLocation> savedLocationItems;
    @XmlElementWrapper(name = "item-items")
    @XmlElement(name = "item")
    public List<SavedItem> itemSavedItems;

    @SuppressWarnings("UnusedDeclaration")
    public SavedGame()
    {
        // default constructor for JAXB
    }

    public SavedGame(Engine.SavedGameHelper savedGameHelper, Story.DataHelper storyDataHelper)
    {
        globalVars = savedGameHelper.getGlobalVariables();
        systemVars = savedGameHelper.getSystemVariables();
        inventory = savedGameHelper.getInventory();
        savedLocationItems = storeSavedLocations(storyDataHelper);
        itemSavedItems = savedGameHelper.getItemItems();
    }

    public List<SavedLocation> storeSavedLocations(Story.DataHelper dataHelper)
    {
        List<SavedLocation> savedLocations = new ArrayList<SavedLocation>();
        for (Location location : dataHelper.getLocations())
        {
            savedLocations.add(new SavedLocation(location));
        }
        return savedLocations;
    }

    public void restoreGame(Engine.SavedGameHelper savedGameHelper, Story.DataHelper storyDataHelper) throws IFML2Exception
    {
        savedGameHelper.setGlobalVariables(globalVars);
        savedGameHelper.setSystemVariables(systemVars);
        savedGameHelper.setInventory(inventory);
        restoreSavedLocations(storyDataHelper);
        savedGameHelper.setItemItems(itemSavedItems);
    }

    private void restoreSavedLocations(Story.DataHelper storyDataHelper)
    {
        for (SavedLocation savedLocation : savedLocationItems)
        {
            savedLocation.restore(storyDataHelper);
        }
    }
}
