package ifml2.engine.saved;

import ifml2.IFML2Exception;
import ifml2.engine.EngineImpl;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Story;
import ifml2.storage.domain.SavedGame;
import ifml2.storage.domain.SavedItem;
import ifml2.storage.domain.SavedLocation;
import ifml2.storage.domain.SavedVariable;
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
public class XmlSavedGame implements SavedGame {

    @XmlTransient
    private static final Logger LOG = LoggerFactory.getLogger(XmlSavedGame.class);

    @XmlAttribute(name = "story-file")
    private String storyFileName;

    @XmlElementWrapper(name = "global-vars")
    @XmlElement(name = "var", type = XmlSavedVariable.class)
    private List<SavedVariable> globalVars;

    @XmlElementWrapper(name = "system-vars")
    @XmlElement(name = "var", type = XmlSavedVariable.class)
    private List<SavedVariable> systemVars;

    @XmlElementWrapper(name = "inventory")
    @XmlElement(name = "item")
    private List<String> savedInventory;

    @XmlElementWrapper(name = "locations")
    @XmlElement(name = "loc", type = XmlSavedLocation.class)
    private List<SavedLocation> savedLocations;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item", type = XmlSavedItem.class)
    private List<SavedItem> savedItems;

    @SuppressWarnings("UnusedDeclaration")
    public XmlSavedGame() {
        // default constructor for JAXB
    }

    public XmlSavedGame(
            EngineImpl.DataHelper engineDataHelper,
            Story.DataHelper storyDataHelper
    ) {
        storyFileName = engineDataHelper.getStoryFileName();
        globalVars = storeGlobalVariables(engineDataHelper);
        systemVars = storeSystemVariables(engineDataHelper);
        savedInventory = storeInventory(engineDataHelper);
        savedLocations = storeSavedLocations(storyDataHelper);
        savedItems = storeSavedItems(storyDataHelper);
    }

    public String getStoryFileName() {
        return storyFileName;
    }

    public void setStoryFileName(final String storyFileName) {
        this.storyFileName = storyFileName;
    }

    public List<SavedVariable> getGlobalVars() {
        return (List<SavedVariable>) globalVars;
    }

    public void setGlobalVars(List<SavedVariable> globalVars) {
        this.globalVars = globalVars;
    }

    public List<SavedVariable> getSystemVars() {
        return (List<SavedVariable>) systemVars;
    }

    public void setSystemVars(List<SavedVariable> systemVars) {
        this.systemVars = systemVars;
    }

    public List<String> getSavedInventory() {
        return savedInventory;
    }

    public void setSavedInventory(List<String> savedInventory) {
        this.savedInventory = savedInventory;
    }

    public List<SavedLocation> getSavedLocations() {
        return (List<SavedLocation>) savedLocations;
    }

    public void setSavedLocations(List<SavedLocation> savedLocations) {
        this.savedLocations = savedLocations;
    }

    public List<SavedItem> getSavedItems() {
        return (List<SavedItem>) savedItems;
    }

    public void setSavedItems(List<SavedItem> savedItems) {
        this.savedItems = savedItems;
    }

    private static List<SavedItem> storeSavedItems(Story.DataHelper dataHelper) {
        List<SavedItem> savedItems = new ArrayList<>();
        for (Item item : dataHelper.getItems()) {
            savedItems.add(new XmlSavedItem(item));
        }
        return savedItems;
    }

    private static List<String> storeInventory(EngineImpl.DataHelper dataHelper) {
        ArrayList<String> itemIds = new ArrayList<>();
        for (Item item : dataHelper.getInventory()) {
            itemIds.add(item.getId());
        }
        return itemIds;
    }

    private static List<SavedVariable> storeSystemVariables(EngineImpl.DataHelper dataHelper) {
        List<SavedVariable> vars = new ArrayList<>();
        for (Map.Entry<String, Value> var : dataHelper.getSystemVariables().entrySet()) {
            vars.add(new XmlSavedVariable(var.getKey(), var.getValue().toLiteral()));
        }
        return vars;
    }

    public static List<SavedVariable> storeGlobalVariables(EngineImpl.DataHelper dataHelper) {
        List<SavedVariable> vars = new ArrayList<>();
        for (Map.Entry<String, Value> var : dataHelper.getGlobalVariables().entrySet()) {
            vars.add(new XmlSavedVariable(var.getKey(), var.getValue().toLiteral()));
        }
        return vars;
    }

    public static List<SavedLocation> storeSavedLocations(Story.DataHelper dataHelper) {
        List<SavedLocation> savedLocations = new ArrayList<>();
        for (Location location : dataHelper.getLocations()) {
            savedLocations.add(new XmlSavedLocation(location));
        }
        return savedLocations;
    }

    private static void restoreSystemVariables(List<SavedVariable> systemVariables, EngineImpl.DataHelper dataHelper) throws IFML2Exception {
        for (SavedVariable var : systemVariables) {
            dataHelper.setSystemVariable(var.getName(), var.getValue());
        }
    }

    private static void restoreGlobalVariables(
            List<SavedVariable> globalVariables,
            EngineImpl.DataHelper dataHelper
    ) throws IFML2Exception {
        for (SavedVariable var : globalVariables) {
            dataHelper.setGlobalVariable(var.getName(), var.getValue());
        }
    }

    private static void restoreSavedLocations(
            List<SavedLocation> savedLocationItems,
            Story.DataHelper storyDataHelper
    ) {
        for (SavedLocation savedLocation : savedLocationItems) {
            ((XmlSavedLocation) savedLocation).restore(storyDataHelper);
        }
    }

    private static void restoreSavedItems(List<SavedItem> savedItems, Story.DataHelper dataHelper) {
        for (SavedItem savedItem : savedItems) {
            ((XmlSavedItem) savedItem).restore(dataHelper);
        }
    }

    private static void restoreInventory(
            List<String> itemIds,
            EngineImpl.DataHelper dataHelper,
            Story.DataHelper storyDataHelper
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
            EngineImpl.DataHelper engineDataHelper,
            Story.DataHelper storyDataHelper
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
