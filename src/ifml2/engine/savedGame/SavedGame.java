package ifml2.engine.savedGame;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "saved-game")
public class SavedGame
{
    private Engine.SavedGameHelper savedGameHelper;

    public SavedGame()
    {
        // default constructor for JAXB
    }

    public SavedGame(Engine.SavedGameHelper savedGameHelper)
    {
        this();
        this.savedGameHelper = savedGameHelper;
    }

    public List<Variable> getGlobalVars()
    {
        return savedGameHelper.getGlobalVariables();
    }

    @XmlElementWrapper(name = "global-vars")
    @XmlElement(name = "var")
    public void setGlobalVars(List<Variable> vars) throws IFML2Exception
    {
        savedGameHelper.setGlobalVariables(vars);
    }

    public List<Variable> getSystemVars()
    {
        return savedGameHelper.getSystemVariables();
    }

    @XmlElementWrapper(name = "system-vars")
    @XmlElement(name = "var")
    public void setSystemVars(List<Variable> vars) throws IFML2Exception
    {
        savedGameHelper.setSystemVariables(vars);
    }

    @XmlElementWrapper(name = "inventory")
    @XmlElement(name = "item")
    public void setInventory(List<String> inventoryIds)
    {
        savedGameHelper.setInventory(inventoryIds);
    }

    public List<String> getInventory()
    {
        return savedGameHelper.getInventory();
    }

    @XmlElementWrapper(name = "locations-items")
    @XmlElement(name = "loc")
    public void setLocItems(List<LocItems> locationsItems)
    {
        savedGameHelper.setLocItems(locationsItems);
    }

    public List<LocItems> getLocItems()
    {
        return savedGameHelper.getLocationsItems();
    }

    @XmlElementWrapper(name = "item-items")
    @XmlElement(name = "item")
    public void setItemItems(List<ItemItems> itemItems)
    {
        //todo savedGameHelper.setItemItems(itemItems);
        throw new NotImplementedException();
    }

    public List<ItemItems> getItemItems()
    {
        //todo return savedGameHelper.getItemItems();
        throw new NotImplementedException();
    }
}
