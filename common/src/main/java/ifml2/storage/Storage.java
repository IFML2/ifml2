package ifml2.storage;

import ifml2.IFML2Exception;

public interface Storage {

    StoryDTO loadStory(String fileName) throws IFML2Exception;

}
