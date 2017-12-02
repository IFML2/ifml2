package ifml2.engine.saved;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Story;

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

    public SavedGame(@NotNull Engine.DataHelper engineDataHelper, @NotNull Story.DataHelper storyDataHelper) {
        storyFileName = engineDataHelper.getStoryFileName();
        globalVars = storeGlobalVariables(engineDataHelper);
        systemVars = storeSystemVariables(engineDataHelper);
        savedInventory = storeInventory(engineDataHelper);
        savedLocations = storeSavedLocations(storyDataHelper);
        savedItems = storeSavedItems(storyDataHelper);
    }

    private static List<SavedItem> storeSavedItems(Story.DataHelper dataHelper) {
        return dataHelper.getItems().stream().map(SavedItem::new).collect(Collectors.toList());
    }

    private static List<String> storeInventory(@NotNull Engine.DataHelper dataHelper) {
        return dataHelper.getInventory().stream().map(IFMLObject::getId).collect(Collectors.toList());
    }

    private static List<SavedVariable> storeSystemVariables(@NotNull Engine.DataHelper dataHelper) {
        return dataHelper.getSystemVariables().entrySet().stream()
                .map(variable -> new SavedVariable(variable.getKey(), variable.getValue().toLiteral()))
                .collect(Collectors.toList());
    }

    public static List<SavedVariable> storeGlobalVariables(@NotNull Engine.DataHelper dataHelper) {
        return dataHelper.getGlobalVariables().entrySet().stream()
                .map(variable -> new SavedVariable(variable.getKey(), variable.getValue().toLiteral()))
                .collect(Collectors.toList());
    }

    public static List<SavedLocation> storeSavedLocations(@NotNull Story.DataHelper dataHelper) {
        return dataHelper.getLocations().stream().map(SavedLocation::new).collect(Collectors.toList());
    }

    private static void restoreSystemVariables(@NotNull List<SavedVariable> systemVariables,
            @NotNull Engine.DataHelper dataHelper) throws IFML2Exception {
        systemVariables.forEach(variable -> {
            try {
                dataHelper.setGlobalVariable(variable.getName(), variable.getValue());
            } catch (IFML2Exception e) {
                // TODO: add logging
            }
        });
    }

    private static void restoreGlobalVariables(@NotNull List<SavedVariable> globalVariables,
            @NotNull Engine.DataHelper dataHelper) throws IFML2Exception {
        globalVariables.forEach(variable -> {
            try {
                dataHelper.setGlobalVariable(variable.getName(), variable.getValue());
            } catch (IFML2Exception e) {
                // TODO: add logging
            }
        });
    }

    private static void restoreSavedLocations(@NotNull List<SavedLocation> savedLocationItems,
            @NotNull Story.DataHelper storyDataHelper) {
        savedLocationItems.forEach(savedLocation -> savedLocation.restore(storyDataHelper));
    }

    private static void restoreSavedItems(List<SavedItem> savedItems, Story.DataHelper dataHelper) {
        savedItems.forEach(savedItem -> savedItem.restore(dataHelper));
    }

    private static void restoreInventory(@NotNull List<String> itemIds, @NotNull Engine.DataHelper dataHelper,
            @NotNull Story.DataHelper storyDataHelper) {
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

    public void restoreGame(@NotNull Engine.DataHelper engineDataHelper, @NotNull Story.DataHelper storyDataHelper)
            throws IFML2Exception {
        // check story file name
        String engineStoryFileName = engineDataHelper.getStoryFileName();
        if (!engineStoryFileName.equalsIgnoreCase(storyFileName)) {
            throw new IFML2Exception(
                    "Файл сохранения не соответсвует текущей истории. Он был сделан для файла \"{0}\".", storyFileName);
        }

        restoreGlobalVariables(globalVars, engineDataHelper);
        restoreSystemVariables(systemVars, engineDataHelper);
        restoreInventory(savedInventory, engineDataHelper, storyDataHelper);
        restoreSavedLocations(savedLocations, storyDataHelper);
        restoreSavedItems(savedItems, storyDataHelper);
    }
}
