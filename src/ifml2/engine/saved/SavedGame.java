package ifml2.engine.saved;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
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
    public List<String> inventoryIds;
    @XmlElementWrapper(name = "locations-items")
    @XmlElement(name = "loc")
    public List<SavedLoc> savedLocItems;
    @XmlElementWrapper(name = "item-items")
    @XmlElement(name = "item")
    public List<SavedItem> itemSavedItems;

    @SuppressWarnings("UnusedDeclaration")
    public SavedGame()
    {
        // default constructor for JAXB
    }

    public SavedGame(Engine.SavedGameHelper savedGameHelper)
    {
        globalVars = savedGameHelper.getGlobalVariables();
        systemVars = savedGameHelper.getSystemVariables();
        inventoryIds = savedGameHelper.getInventory();
        savedLocItems = savedGameHelper.getLocationsItems();
        itemSavedItems = savedGameHelper.getItemItems();
    }

    public void restoreGame(Engine.SavedGameHelper savedGameHelper) throws IFML2Exception
    {
        savedGameHelper.setGlobalVariables(globalVars);
        savedGameHelper.setSystemVariables(systemVars);
        savedGameHelper.setInventory(inventoryIds);
        savedGameHelper.setLocItems(savedLocItems);
        savedGameHelper.setItemItems(itemSavedItems);
    }

    public List<String> getInventory()
    {
        return inventoryIds;
    }

    public void setInventory(List<String> inventoryIds)
    {
        this.inventoryIds = inventoryIds;
    }
}
