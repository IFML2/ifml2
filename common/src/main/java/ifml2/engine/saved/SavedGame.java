package ifml2.engine.saved;

import ifml2.IFML2Exception;
import ifml2.engine.EngineImpl;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Story;
import ifml2.vm.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "saved-game")
public class SavedGame {
    @XmlTransient
    private static final Logger LOG = LoggerFactory.getLogger(SavedGame.class);
    @XmlAttribute(name = "story-file")
    public String storyFileName;
    @XmlElementWrapper(name = "global-vars")
    @XmlElement(name = "var")
    public List<SavedVariable> globalVars;
    @XmlElementWrapper(name = "system-vars")
    @XmlElement(name = "var")
    public List<SavedVariable> systemVars;
    @XmlElementWrapper(name = "inventory")
    @XmlElement(name = "item")
    public List<String> savedInventory;
    @XmlElementWrapper(name = "locations")
    @XmlElement(name = "loc")
    public List<SavedLocation> savedLocations;
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<SavedItem> savedItems;

    @SuppressWarnings("UnusedDeclaration")
    public SavedGame() {
        // default constructor for JAXB
    }

    public SavedGame(
            /*@NotNull*/ EngineImpl.DataHelper engineDataHelper,
            /*@NotNull*/ Story.DataHelper storyDataHelper
    ) {
        storyFileName = engineDataHelper.getStoryFileName();
        globalVars = storeGlobalVariables(engineDataHelper);
        systemVars = storeSystemVariables(engineDataHelper);
        savedInventory = storeInventory(engineDataHelper);
        savedLocations = storeSavedLocations(storyDataHelper);
        savedItems = storeSavedItems(storyDataHelper);
    }

    private static List<SavedItem> storeSavedItems(Story.DataHelper dataHelper) {
        ArrayList<SavedItem> savedItems = new ArrayList<SavedItem>();
        for (Item item : dataHelper.getItems()) {
            savedItems.add(new SavedItem(item));
        }
        return savedItems;
    }

    private static List<String> storeInventory(/*@NotNull*/ EngineImpl.DataHelper dataHelper) {
        ArrayList<String> itemIds = new ArrayList<String>();
        for (Item item : dataHelper.getInventory()) {
            itemIds.add(item.getId());
        }
        return itemIds;
    }

    private static List<SavedVariable> storeSystemVariables(/*@NotNull*/ EngineImpl.DataHelper dataHelper) {
        List<SavedVariable> vars = new ArrayList<SavedVariable>();
        for (Map.Entry<String, Value> var : dataHelper.getSystemVariables().entrySet()) {
            vars.add(new SavedVariable(var.getKey(), var.getValue().toLiteral()));
        }
        return vars;
    }

    public static List<SavedVariable> storeGlobalVariables(/*@NotNull*/ EngineImpl.DataHelper dataHelper) {
        List<SavedVariable> vars = new ArrayList<SavedVariable>();
        for (Map.Entry<String, Value> var : dataHelper.getGlobalVariables().entrySet()) {
            vars.add(new SavedVariable(var.getKey(), var.getValue().toLiteral()));
        }
        return vars;
    }

    public static List<SavedLocation> storeSavedLocations(/*@NotNull*/ Story.DataHelper dataHelper) {
        List<SavedLocation> savedLocations = new ArrayList<SavedLocation>();
        for (Location location : dataHelper.getLocations()) {
            savedLocations.add(new SavedLocation(location));
        }
        return savedLocations;
    }

    private static void restoreSystemVariables(
            /*@NotNull*/ List<SavedVariable> systemVariables,
            /*@NotNull*/ EngineImpl.DataHelper dataHelper
    ) throws IFML2Exception {
        for (SavedVariable var : systemVariables) {
            dataHelper.setSystemVariable(var.getName(), var.getValue());
        }
    }

    private static void restoreGlobalVariables(
            /*@NotNull*/ List<SavedVariable> globalVariables,
            /*@NotNull*/ EngineImpl.DataHelper dataHelper
    ) throws IFML2Exception {
        for (SavedVariable var : globalVariables) {
            dataHelper.setGlobalVariable(var.getName(), var.getValue());
        }
    }

    private static void restoreSavedLocations(
            /*@NotNull*/ List<SavedLocation> savedLocationItems,
            /*@NotNull*/ Story.DataHelper storyDataHelper
    ) {
        for (SavedLocation savedLocation : savedLocationItems) {
            savedLocation.restore(storyDataHelper);
        }
    }

    private static void restoreSavedItems(List<SavedItem> savedItems, Story.DataHelper dataHelper) {
        for (SavedItem savedItem : savedItems) {
            savedItem.restore(dataHelper);
        }
    }

    private static void restoreInventory(
            /*@NotNull*/ List<String> itemIds,
            /*@NotNull*/ EngineImpl.DataHelper dataHelper,
            /*@NotNull*/ Story.DataHelper storyDataHelper
    ) {
        List<Item> inventory = dataHelper.getInventory();
        inventory.clear();
        for (String id : itemIds) {
            Item item = storyDataHelper.findItemById(id);
            if (item != null) {
                inventory.add(item);
                item.setContainer(inventory); // todo refactor to set in OM in one action
            } else {
                LOG.warn("[Game loading] Inventory loading: there is no item with id \"{0}\".", id);
            }
        }
    }

    public void restoreGame(
            /*@NotNull*/ EngineImpl.DataHelper engineDataHelper,
            /*@NotNull*/ Story.DataHelper storyDataHelper
    ) throws IFML2Exception {
        // check story file name
        String engineStoryFileName = engineDataHelper.getStoryFileName();
        if (!engineStoryFileName.equalsIgnoreCase(storyFileName)) {
            throw new IFML2Exception("Файл сохранения не соответсвует текущей истории. Он был сделан для файла \"{0}\".", storyFileName);
        }

        restoreGlobalVariables(globalVars, engineDataHelper);
        restoreSystemVariables(systemVars, engineDataHelper);
        restoreInventory(savedInventory, engineDataHelper, storyDataHelper);
        restoreSavedLocations(savedLocations, storyDataHelper);
        restoreSavedItems(savedItems, storyDataHelper);
    }
}
