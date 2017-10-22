package ifml2.storage;

import ifml2.IFML2Exception;
import ifml2.engine.saved.XmlSavedGame;
import ifml2.om.OMManager;
import ifml2.om.Story;
import ifml2.storage.domain.SavedGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class StorageImpl implements Storage {

    private final Logger logger = LoggerFactory.getLogger(StorageImpl.class);

    public StoryDTO loadStory(String fileName) throws IFML2Exception {
        logger.info("Загружаем историю {} ...", fileName);

        File file = new File(fileName);

        if (!file.exists()) {
            logger.error("История {} - не найдена...", fileName);
            throw new IFML2Exception("История не найдена");
        }

        return OMManager.loadStoryFromFile(fileName, true, true);
    }

    @Override
    public void saveGame(String fileName, SavedGame savedGame) throws IFML2Exception {
        try {
            JAXBContext context = JAXBContext.newInstance(XmlSavedGame.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(fileName);

            marshaller.marshal(savedGame, file);
        } catch (JAXBException e) {
            throw new IFML2Exception(e);
        }
    }

    @Override
    public SavedGame loadGame(String fileName) throws IFML2Exception {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                JAXBContext context = JAXBContext.newInstance(XmlSavedGame.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (SavedGame) unmarshaller.unmarshal(file);
            } else {
                throw new IFML2Exception("File \"{0}\" not found.", fileName);
            }
        } catch (JAXBException e) {
            throw new IFML2Exception(e);
        }
    }

}
