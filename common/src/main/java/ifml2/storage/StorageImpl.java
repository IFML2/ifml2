package ifml2.storage;

import ifml2.IFML2Exception;
import ifml2.engine.SystemCommand;
import ifml2.om.OMManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class StorageImpl implements Storage {

    private final Logger logger = LoggerFactory.getLogger(StorageImpl.class);

    @Override
    public StoryDTO loadStory(String fileName) throws IFML2Exception {
        logger.info("Загружаем историю {} ...", fileName);

        File file = new File(fileName);

        if (!file.exists()) {
            logger.error("История {} - не найдена...", fileName);
            throw new IFML2Exception("История не найдена");
        }

        return OMManager.loadStoryFromFile(fileName, true, true);
    }

}
