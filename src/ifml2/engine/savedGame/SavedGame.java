package ifml2.engine.savedGame;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "saved-game")
public class SavedGame
{
    private List<Variable> globalVars;
    private List<Variable> systemVars;
    private List<String> inventoryIds;
    private List<LocItems> locItems;
    private List<ItemItems> itemItems;

    public SavedGame()
    {
        // default constructor for JAXB
    }

    public SavedGame(Engine.SavedGameHelper savedGameHelper)
    {
        this();
        globalVars = savedGameHelper.getGlobalVariables();
        systemVars = savedGameHelper.getSystemVariables();
        inventoryIds = savedGameHelper.getInventory();
        locItems = savedGameHelper.getLocationsItems();
        itemItems = savedGameHelper.getItemItems();
    }

    public void restoreGame(Engine.SavedGameHelper savedGameHelper) throws IFML2Exception
    {
        savedGameHelper.setGlobalVariables(globalVars);
        savedGameHelper.setSystemVariables(systemVars);
        savedGameHelper.setInventory(inventoryIds);
        savedGameHelper.setLocItems(locItems);
        savedGameHelper.setItemItems(itemItems);
    }

    public List<Variable> getGlobalVars()
    {
        //return savedGameHelper.getGlobalVariables();
        return globalVars;
    }

    @XmlElementWrapper(name = "global-vars")
    @XmlElement(name = "var")
    public void setGlobalVars(List<Variable> globalVars) throws IFML2Exception
    {
        //savedGameHelper.setGlobalVariables(vars);
        this.globalVars = globalVars;
    }

    public List<Variable> getSystemVars()
    {
        //return savedGameHelper.getSystemVariables();
        return systemVars;
    }

    @XmlElementWrapper(name = "system-vars")
    @XmlElement(name = "var")
    public void setSystemVars(List<Variable> systemVars) throws IFML2Exception
    {
        //savedGameHelper.setSystemVariables(vars);
        this.systemVars = systemVars;
    }

    @XmlElementWrapper(name = "inventory")
    @XmlElement(name = "item")
    public void setInventory(List<String> inventoryIds)
    {
        //savedGameHelper.setInventory(inventoryIds);
        this.inventoryIds = inventoryIds;
    }

    public List<String> getInventory()
    {
        //return savedGameHelper.getInventory();
        return inventoryIds;
    }

    @XmlElementWrapper(name = "locations-items")
    @XmlElement(name = "loc")
    public void setLocItems(List<LocItems> locationsItems)
    {
        //savedGameHelper.setLocItems(locationsItems);
        this.locItems = locationsItems;
    }

    public List<LocItems> getLocItems()
    {
        //return savedGameHelper.getLocationsItems();
        return locItems;
    }

    @XmlElementWrapper(name = "item-items")
    @XmlElement(name = "item")
    public void setItemItems(List<ItemItems> itemItems)
    {
        //savedGameHelper.setItemItems(itemItems);
        //throw new NotImplementedException();
        this.itemItems = itemItems;
    }

    public List<ItemItems> getItemItems()
    {
        //return savedGameHelper.getItemItems();
        //throw new NotImplementedException();
        return itemItems;
    }
}
