package ifml2.storage;

import ifml2.om.Item;
import ifml2.om.Story;

import java.util.List;

public class StoryDTO {

    private final Story story;
    private final List<Item> inventory;

    public StoryDTO(final Story story, final List<Item> inventory) {
        this.story = story;
        this.inventory = inventory;
    }

    public Story getStory() {
        return story;
    }

    public List<Item> getInventory() {
        return inventory;
    }

}
