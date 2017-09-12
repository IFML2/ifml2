package ifml2.om;

import com.sun.xml.internal.bind.IDResolver;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.engine.saved.SavedGame;
import ifml2.om.xml.xmladapters.LocationAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.util.ValidationEventCollector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class OMManager {
    public static final Logger LOG = LoggerFactory.getLogger(OMManager.class);

    /**
     * Loads story from xml file
     *
     * @param storyFileName            Full path to xml file with story.
     * @param toInitItemsStartLoc      Provide true if items should be copied into start positions (inventory and locations).
     *                                 It's necessary in Editor.
     * @param isAllowedOpenCipherFiles Provide true if it's allowed to load ciphered stories (from Players) or false if not (from Editor).
     * @return Wrapped result containing story and loaded inventory (see toInitItemsStartLoc param).
     * @throws IFML2Exception If some error has occurred during loading.
     */
    public static LoadStoryResult loadStoryFromFile(@NotNull String storyFileName, final boolean toInitItemsStartLoc, boolean isAllowedOpenCipherFiles) throws IFML2Exception {
        LOG.debug("loadStoryFromFile(storyFileName = \"{0}\", toInitItemsStartLoc = {1}) :: begin", storyFileName, toInitItemsStartLoc);

        InputStream inputStream = null;
        try {
            try {
                // detect ciphered story by extension
                if (storyFileName.trim().endsWith(CommonConstants.CIPHERED_STORY_EXTENSION)) {
                    LOG.debug("loadStoryFromFile :: File is ciphered, decipher...");

                    if (!isAllowedOpenCipherFiles) {
                        throw new IFML2Exception("В этом режиме нельзя открывать зашифрованные истории!");
                    }

                    try (FileInputStream cipheredFile = new FileInputStream(storyFileName)) {
                        // read key length
                        int keyLength = cipheredFile.read();
                        // read cipher key from file
                        byte[] keyBytes = new byte[keyLength];
                        int bytesRead = cipheredFile.read(keyBytes);
                        if (bytesRead == -1) {
                            LOG.error("loadStoryFromFile :: file stream read() for  returned {0}", bytesRead);
                            throw new IFML2Exception("Неожиданно короткий файл {0}.", storyFileName);
                        }

                        // create key
                        SecretKey cipherKey = new SecretKeySpec(keyBytes, "DES");

                        // Initialize the same cipher for decryption
                        Cipher desCipher = CommonUtils.createCipher();
                        desCipher.init(Cipher.DECRYPT_MODE, cipherKey);

                        byte[] cipherBytes = new byte[cipheredFile.available()];
                        bytesRead = cipheredFile.read(cipherBytes);
                        LOG.debug("loadStoryFromFile :: file stream read() returned {0}", bytesRead);

                        // Decrypt the story
                        byte[] textDecrypted = desCipher.doFinal(cipherBytes);

                        // write deciphered story to stream
                        inputStream = new ByteArrayInputStream(textDecrypted);
                    }
                } else {
                    LOG.debug("loadStoryFromFile :: File is normal, read...");
                    inputStream = new FileInputStream(storyFileName);
                }

                final Story story;
                final ArrayList<Item> inventory = new ArrayList<Item>();

                JAXBContext context = JAXBContext.newInstance(Story.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                unmarshaller.setProperty(IDResolver.class.getName(), new IFMLIDResolver());
                unmarshaller.setAdapter(new LocationAdapter());

                final HashMap<String, IFMLObject> ifmlObjectsHeap = new HashMap<String, IFMLObject>();

                unmarshaller.setListener(new Unmarshaller.Listener() {
                    @Override
                    public void afterUnmarshal(Object target, Object parent) {
                        LOG.debug("afterUnmarshal({0}, {1})", target, parent);

                        if (target instanceof IFMLObject) {
                            IFMLObject ifmlObject = (IFMLObject) target;

                            // load all objects into objectsHeap
                            ifmlObjectsHeap.put(ifmlObject.getId().toLowerCase(), ifmlObject);

                            // add item to inventory by starting position
                            if (toInitItemsStartLoc && ifmlObject instanceof Item) {
                                Item item = (Item) ifmlObject;
                                if (item.getStartingPosition().getInventory()) {
                                    inventory.add(item); //should it be original items
                                    item.setContainer(inventory);
                                }
                            }
                        }
                    }
                });

                //LOG.debug("loadStoryFromFile :: File object for path \"{0}\" created", file.getAbsolutePath());

                ValidationEventCollector validationEventCollector = new ValidationEventCollector() {
                    @Override
                    public boolean handleEvent(ValidationEvent event) {
                        LOG.warn("There is ValidationEvent during unmarshalling: {0}", event);
                        return super.handleEvent(event);
                    }
                };
                unmarshaller.setEventHandler(validationEventCollector);

                LOG.debug("loadStoryFromFile :: before unmarshal");
                story = (Story) unmarshaller.unmarshal(inputStream);
                LOG.debug("loadStoryFromFile :: after unmarshal");

                addWordReverseLinks(
                        ifmlObjectsHeap); // adding links is made explicitly because WordLinks in unmarshal listeners are not loaded with words yet

                if (validationEventCollector.hasEvents()) {
                    throw new IFML2LoadXmlException(validationEventCollector.getEvents());
                }

                story.setObjectsHeap(ifmlObjectsHeap);

                if (toInitItemsStartLoc) {
                    assignItemsToLocations(story);
                }

                LOG.debug("loadStoryFromFile :: End");

                return new LoadStoryResult(story, inventory);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            throw new IFML2Exception(e, "Ошибка при загрузке истории: {0}", e.getMessage());
        }
    }

    private static void addWordReverseLinks(HashMap<String, IFMLObject> storyObjectsHeap) throws IFML2Exception {
        // add reverse links
        for (IFMLObject object : storyObjectsHeap.values()) {
            WordLinks wordLinks = object.getWordLinks();
            Word mainWord = wordLinks.getMainWord();
            if (object instanceof Item && mainWord == null) {
                throw new IFML2Exception("Основное слово не задано у объекта {0}", object);
            }
            if (mainWord != null) {
                LOG.debug("setWordLinks() :: Adding link for main word \"{0}\" to object \"{1}\"", mainWord, object);
                mainWord.addLinkerObject(object);
            }
            for (Word word : wordLinks.getWords()) {
                if (word == null) {
                    throw new IFML2Exception("Задана неверная ссылка на слово у объекта {0}", object);
                }

                LOG.debug("setWordLinks() :: Adding link for word \"{0}\" to object \"{1}\"", mainWord, object);
                word.addLinkerObject(object);
            }
        }
    }

    private static void assignItemsToLocations(Story story) {
        for (Item item : story.getItems()) {
            for (Location location : item.getStartingPosition().getLocations()) {
                List<Item> items = location.getItems();
                items.add(item);
                item.setContainer(items);
            }
        }
    }

    public static Library loadLibrary(String libPath) throws IFML2Exception {
        LOG.debug("loadLibrary(String libPath = \"{0}\")", libPath);

        //--Loading from JAR--Reader reader = new BufferedReader(new InputStreamReader(OMManager.class.getResourceAsStream(realRelativePath), "UTF-8"));

        File file = new File(CommonConstants.LIBS_FOLDER, libPath);
        LOG.debug("loadLibrary :: real relative path = \"{0}\"", file.getAbsolutePath());

        Library library = loadLibrary(file);
        LOG.debug("loadLibrary(String) :: End");

        return library;
    }

    public static Library loadLibrary(@NotNull final File libFile) throws IFML2Exception {
        LOG.debug("loadLibrary(File libFile = \"{0}\")", libFile.getAbsolutePath());

        Library library;

        try {
            //todo remove LOG.debug("loadLibrary :: File object for path \"{0}\" created", libFile.getAbsolutePath());

            if (!libFile.exists()) {
                LOG.error("loadLibrary :: Library file \"{0}\" isn't found!", libFile.getAbsolutePath());
                throw new IFML2Exception("Файл \"{0}\" библиотеки не найдена", libFile.getAbsolutePath());
            }

            JAXBContext context = JAXBContext.newInstance(Library.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            //todo: modify IDResolver for library hierarchical loading --  unmarshaller.setProperty(IDResolver.class.getName(), new IFMLIDResolver());

            ValidationEventCollector validationEventCollector = new ValidationEventCollector() {
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    LOG.warn("There is ValidationEvent during unmarshalling of library {0}: {1}", libFile.getAbsolutePath(), event);
                    return super.handleEvent(event);
                }
            };
            unmarshaller.setEventHandler(validationEventCollector);

            LOG.debug("loadLibrary :: before unmarshal");
            library = (Library) unmarshaller.unmarshal(libFile);
            LOG.debug("loadLibrary :: after unmarshal");

            String librariesDirectory = CommonUtils.getLibrariesDirectory();
            LOG.debug("librariesDirectory = {0}", librariesDirectory);

            //todo rewrite using Path in Java >= 7
            File libFolder = new File(librariesDirectory);
            URI libFileUri = new URI(libFile.toURI().toString().toLowerCase());
            LOG.debug("libFile.URI = {0}", libFileUri);
            URI libFolderUri = new URI(libFolder.toURI().toString().toLowerCase());
            LOG.debug("libFolderUri.URI = {0}", libFolderUri);
            URI relativeUri = libFolderUri.relativize(libFileUri);
            LOG.debug("relativeUri = {0}", relativeUri);
            String libPath = relativeUri.getPath();
            LOG.debug("loadLibrary :: calculated libPath = {0}", libPath);
            library.setPath(libPath);
        } catch (JAXBException e) {
            throw new IFML2Exception(e, "Ошибка загрузки библиотеки: {0}", e.getMessage());
        } catch (URISyntaxException e) {
            throw new IFML2Exception(e, "Ошибка вычисления относительного пути.");
        }

        LOG.debug("loadLibrary(File) :: End");

        return library;
    }

    public static void saveStoryToXmlFile(String xmlFile, Story story) throws IFML2Exception {
        try {
            JAXBContext context = JAXBContext.newInstance(Story.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setAdapter(new LocationAdapter());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            File file = new File(xmlFile);

            marshaller.marshal(story, file);
        } catch (JAXBException e) {
            throw new IFML2Exception(e);
        }
    }

    public static void saveGame(String saveFileName, SavedGame savedGame) throws IFML2Exception {
        try {
            JAXBContext context = JAXBContext.newInstance(SavedGame.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File file = new File(saveFileName);

            marshaller.marshal(savedGame, file);
        } catch (JAXBException e) {
            throw new IFML2Exception(e);
        }
    }

    public static SavedGame loadGame(String saveFileName) throws IFML2Exception {
        try {
            File file = new File(saveFileName);
            if (file.exists()) {
                JAXBContext context = JAXBContext.newInstance(SavedGame.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (SavedGame) unmarshaller.unmarshal(file);
            } else {
                throw new IFML2Exception("Файл \"{0}\" не существует.", saveFileName);
            }
        } catch (JAXBException e) {
            throw new IFML2Exception(e);
        }
    }

    public static void exportCipheredStory(String fileName, Story story) throws IFML2Exception {
        try {
            JAXBContext context = JAXBContext.newInstance(Story.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                // marshal to bytes
                marshaller.marshal(story, byteStream);
                byte[] storyBytes = byteStream.toByteArray();

                // Create the cipher
                KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
                Cipher desCipher = CommonUtils.createCipher();
                // Initialize the cipher for encryption
                SecretKey key = keygenerator.generateKey();
                desCipher.init(Cipher.ENCRYPT_MODE, key);

                // Encrypt the story
                byte[] storyBytesEncrypted = desCipher.doFinal(storyBytes);

                // create fileOutputStream
                FileOutputStream cipherStoryFile = new FileOutputStream(fileName);
                try {
                    // store key
                    byte[] keyBytes = key.getEncoded();
                    cipherStoryFile.write(keyBytes.length); // key length
                    cipherStoryFile.write(keyBytes); // key itself

                    // write cipher
                    cipherStoryFile.write(storyBytesEncrypted);
                } finally {
                    cipherStoryFile.close();
                }
            } finally {
                byteStream.close();
            }
        } catch (Exception e) {
            throw new IFML2Exception(e);
        }
    }

    public static void saveLib(Library library, File libFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Library.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(library, libFile);
    }

    public static class LoadStoryResult {
        private final Story story;
        private final ArrayList<Item> inventory;

        public LoadStoryResult(Story story, ArrayList<Item> inventory) {
            this.story = story;
            this.inventory = inventory;
        }

        public Story getStory() {
            return story;
        }

        public ArrayList<Item> getInventory() {
            return inventory;
        }
    }

    private static class IFMLIDResolver extends IDResolver {
        public static final Logger LOG = LoggerFactory.getLogger(IFMLIDResolver.class);
        private final BindingMap bindings = new BindingMap();
        private Story story;

        @Override
        public void startDocument(ValidationEventHandler validationEventHandler) throws SAXException {
            LOG.debug("startDocument()");
            super.startDocument(validationEventHandler);
            bindings.clear();
            story = null;
        }

        @Override
        public void bind(String s, Object o) throws SAXException {
            LOG.debug("bind(s = \"{0}\", o = \"{1}\"); class of o is {2}", s, o, o != null ? o.getClass() : "[o is null!]");

            // save link to story
            if (o instanceof Story) {
                LOG.debug("bind() :: parameter Object o is Story, saving: {0}", o);
                story = (Story) o;
            } else {
                bindings.addBinding(s, o);
            }
        }

        @Override
        public Callable<?> resolve(final String s, final Class aClass) throws SAXException {
            LOG.debug("resolve(s = \"{0}\", aClass = \"{1}\")", s, aClass);

            return new Callable<Object>() {
                public Object call() throws Exception {
                    LOG.debug("call() :: Trying to resolve \"{0}\" for \"{1}\" class", s, aClass);

                    if (bindings.containsKeyOfClass(s, aClass)) {
                        Object object = bindings.getObjectOfClass(s, aClass);
                        LOG.debug("call() ::    => binding \"{0}\"; class is {1}", object,
                                object != null ? object.getClass() : "[o is null!]");
                        return object;
                    } else {
                        LOG.debug("call() ::   not found in bindings, trying to find in libs: ...");
                        // try to find key in libraries
                        if (story != null) {
                            LOG.debug("call() ::    story is not null, trying to get story.getLibraries(). " + "getLibraries() returns {0}",
                                    story.getLibraries());
                            for (Object object : story.getLibraries()) {
                                LOG.debug("call() ::   test object class from getLibraries(): {0}", object.getClass());
                                if (!(object instanceof Library)) {
                                    throw new IFML2Exception("Member of getLibraries isn't a Library! It's {0}", object.getClass());
                                }
                                Library library = (Library) object;
                                LOG.debug("call() ::   - searching in lib {0}, class is {1}", library.getName(), aClass);

                                if (aClass == Object.class) {
                                    LOG.warn("call() :: aClass is Object for name \"{0}\"!", s);
                                }

                                // attributes
                                if (aClass == Attribute.class || aClass == Object.class) //todo: remove Object after JAXB fix of JAXB-546
                                {
                                    LOG.debug("call() ::   => searching Attribute \"{0}\"", s);
                                    Attribute attribute = library.getAttributeByName(s);
                                    if (attribute != null) {
                                        LOG.debug("call() ::     - found Attribute: \"{0}\"", attribute);
                                        return attribute;
                                    }
                                }
                                // actions
                                else if (aClass == Action.class || aClass == Object.class) //todo: remove Object after JAXB fix of JAXB-546
                                {
                                    LOG.debug("call() ::   => searching Action \"{0}\"", s);
                                    Action action = library.getActionByName(s);
                                    if (action != null) {
                                        LOG.debug("call() ::     - found Action: \"{0}\"", action);
                                        return action;
                                    }
                                }
                                // role definitions
                                else if (aClass == RoleDefinition.class || aClass == Object.class) {
                                    LOG.debug("call() ::   => searching RoleDefinition \"{0}\"", s);
                                    RoleDefinition roleDefinition = library.getRoleDefinitionByName(s);
                                    if (roleDefinition != null) {
                                        LOG.debug("call() ::     - found RoleDefinition: \"{0}\"", roleDefinition);
                                        return roleDefinition;
                                    }
                                } else {
                                    LOG.debug("call() ::     it's nor Attribute, nor Action, nor RoleDefinition => nothing to search else");
                                }
                            }
                        } else {
                            LOG.debug("call() ::    story is null");
                        }
                    }
                    LOG.debug("call() ::   -> Binding NOT FOUND");
                    return null;
                }
            };
        }

        @Override
        public void endDocument() throws SAXException {
            LOG.debug("endDocument()");
            super.endDocument();
        }

        private class BindingMap extends HashMap<String, ArrayList<Object>> {
            public final Logger LOG = LoggerFactory.getLogger(BindingMap.class);

            public void addBinding(String name, Object object) {
                // add name in lower case
                String loweredName = name.toLowerCase();

                if (containsKey(loweredName)) {
                    get(loweredName).add(object);
                } else {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    arrayList.add(object);
                    put(loweredName, arrayList);
                }
            }

            public boolean containsKeyOfClass(String name, Class<?> aClass) {
                String loweredName = name.toLowerCase();

                if (containsKey(loweredName)) {
                    for (Object object : get(loweredName)) {
                        if (object != null) {
                            Class objectClass = object.getClass();
                            if (/*objectClass.equals(aClass) ||*/ aClass.isAssignableFrom(objectClass) /*|| aClass == Object.class*/) //todo remove Object after JAXB fix of JAXB-546
                            {
                                if (aClass == Object.class) {
                                    LOG.warn("containsKeyOfClass() :: returns true for \"{0}\" when aClass is Object!", name);
                                }
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            public Object getObjectOfClass(String name, Class<?> aClass) {
                String loweredName = name.toLowerCase();

                if (containsKey(loweredName)) {
                    for (Object object : get(loweredName)) {
                        if (object != null) {
                            Class<?> objectClass = object.getClass();
                            if (aClass.isAssignableFrom(objectClass) /*objectClass.equals(aClass) || aClass == Object.class*/) //todo remove Object after JAXB fix of JAXB-546
                            {
                                if (aClass == Object.class) {
                                    LOG.warn("getObjectOfClass() :: returns object \"{0}\" for \"{0}\" when aClass is Object!", object, name);
                                }
                                return object;
                            }
                        }
                    }
                }
                return null;
            }
        }
    }
}
