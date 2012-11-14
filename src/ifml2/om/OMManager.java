package ifml2.om;

import com.sun.xml.internal.bind.IDResolver;
import ifml2.FormatLogger;
import ifml2.IFML2Exception;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.bind.util.ValidationEventCollector;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class OMManager
{
    public static final FormatLogger LOG = FormatLogger.getLogger(OMManager.class);

    /**
     * Loads story from xml file
     * @param xmlFile Full path to xml file with story.
     * @param toInitItemsStartLoc Provide true if items should be copied into start positions (inventory and locations).
     * It's necessary in Editor.
     * @return Wrapped result containing story and loaded inventory (see toInitItemsStartLoc param).
     * @throws IFML2Exception If some error has occurred during loading.
     */
    public static LoadStoryResult loadStoryFromXmlFile(String xmlFile, final boolean toInitItemsStartLoc) throws IFML2Exception
    {
        LOG.debug("loadStoryFromXmlFile(xmlFile = \"{0}\", toInitItemsStartLoc = {1}) :: begin", xmlFile, toInitItemsStartLoc);
        final Story story;
        final ArrayList<Item> inventory = new ArrayList<Item>();

		try
		{
			JAXBContext context = JAXBContext.newInstance(Story.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setProperty(IDResolver.class.getName(), new IFMLIDResolver());

            final HashMap<String, IFMLObject> ifmlObjectsHeap = new HashMap<String, IFMLObject>();

            unmarshaller.setListener(new Unmarshaller.Listener()
            {
                @Override
                public void afterUnmarshal(Object target, Object parent)
                {
                    // load all objects into objectsHeap
                    if (target instanceof IFMLObject)
                    {
                        IFMLObject ifmlObject = (IFMLObject) target;
                        ifmlObjectsHeap.put(ifmlObject.getId().toLowerCase(), ifmlObject);

                        // add item to inventory by starting position
                        if (toInitItemsStartLoc && ifmlObject instanceof Item)
                        {
                            Item item = (Item) ifmlObject;
                            if (item.getStartingPosition().getInventory())
                            {
                                inventory.add(item); //should it be original items
                            }
                        }
                    }
                }
            });

	        File file = new File(xmlFile);
            LOG.debug("loadStoryFromXmlFile :: File object for path \"{0}\" created", file.getAbsolutePath());

            ValidationEventCollector validationEventCollector = new ValidationEventCollector()
            {
                @Override
                public boolean handleEvent(ValidationEvent event)
                {
                    LOG.warn("There is ValidationEvent during unmarshalling: {0}", event);
                    return super.handleEvent(event);
                }
            };
            unmarshaller.setEventHandler(validationEventCollector);

            LOG.debug("loadStoryFromXmlFile :: before unmarshal");
            story = (Story) unmarshaller.unmarshal(file);
            LOG.debug("loadStoryFromXmlFile :: after unmarshal");

            if(validationEventCollector.hasEvents())
            {
                throw new IFML2LoadXmlException(validationEventCollector.getEvents());
            }

            story.setObjectsHeap(ifmlObjectsHeap);

            if(toInitItemsStartLoc)
            {
                assignItemsToLocations(story);
            }
            assignLinksWordsToObjects(story);

            LOG.debug("loadStoryFromXmlFile :: End");
		}
		catch (JAXBException e)
        {
            throw new IFML2Exception(e, "Ошибка при загрузке истории: {0}", e.getMessage());
        }

		return new LoadStoryResult(story, inventory);
	}

    private static void assignItemsToLocations(Story story)
    {
        for(Item item : story.getItems())
        {
            for(Location location : item.getStartingPosition().getLocations())
            {
                location.getItems().add(item);
            }
        }
    }

    private static void assignLinksWordsToObjects(Story story) throws IFML2Exception
    {
        for(IFMLObject ifmlObject : story.getObjectsHeap().values())
        {
            WordLinks wordLinks = ifmlObject.getWordLinks();

            if(wordLinks == null)
            {
                throw new IFML2Exception("Список ссылок на слова не задан у объекта {0}", ifmlObject);
            }

            /*if(wordLinks.mainWord == null)
            {
                throw new IFML2Exception("Основное слово не задано у объекта {0}", ifmlObject);
            }*/

            if(wordLinks.getMainWord() != null)
            {
                wordLinks.getMainWord().linkerObjects.add(ifmlObject);
            }

            for(Word word : wordLinks.getWords())
            {
                if(word == null)
                {
                    throw new IFML2Exception("Задана неверная ссылка на слово у объекта {0}", ifmlObject);
                }

                if(!word.linkerObjects.contains(ifmlObject))
                {
                    word.linkerObjects.add(ifmlObject);
                }
            }
        }
    }

    public static Library loadLibrary(String libPath) throws IFML2Exception
	{
        LOG.debug(String.format("loadLibrary(libPath = \"%s\")" , libPath));

        Library library;

		try
		{
			JAXBContext context = JAXBContext.newInstance(Library.class);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
            //todo:unmarshaller.setProperty(IDResolver.class.getName(), new IFMLIDResolver());

            // TODO: загрузка стандартных и прочих либ

	        String realRelativePath = "libs/" + libPath; // for JAR should be from root: "/libs/:
            LOG.debug("loadLibrary :: realRelativePath = \"{0}\"", realRelativePath);

            //--Loading from JAR--Reader reader = new BufferedReader(new InputStreamReader(OMManager.class.getResourceAsStream(realRelativePath), "UTF-8"));

            File file = new File(realRelativePath);
            LOG.debug("loadLibrary :: File object for path \"{0}\" created", file.getAbsolutePath());

	        if(!file.exists())
	        {
                LOG.error("loadLibrary :: Library file \"{0}\" isn't found!", file.getAbsolutePath());
                throw new IFML2Exception("Файл \"{0}\" библиотеки не найдена", file.getAbsolutePath());
	        }

            ValidationEventCollector validationEventCollector = new ValidationEventCollector()
            {
                @Override
                public boolean handleEvent(ValidationEvent event)
                {
                    //return super.handleEvent(event);    //To change body of overridden methods use File | Settings | File Templates.
                    return false;
                }
            };
            unmarshaller.setEventHandler(validationEventCollector);

            LOG.debug(String.format("loadLibrary :: before unmarshal"));
            library = (Library) unmarshaller.unmarshal(file);
            LOG.debug(String.format("loadLibrary :: after unmarshal"));
            library.setPath(libPath);
		}
		catch (JAXBException e)
		{
			throw new IFML2Exception(e);
		}

        LOG.debug(String.format("loadLibrary :: End"));

		return library;
	}

    public static void saveStoryToXmlFile(String xmlFile, Story story) throws IFML2Exception
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(Story.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            File file = new File(xmlFile);

            marshaller.marshal(story, file);
        }
        catch (JAXBException e)
        {
            throw new IFML2Exception(e);
        }
    }

    public static class LoadStoryResult
    {
        private final Story story;
        private final ArrayList<Item> inventory;

        public LoadStoryResult(Story story, ArrayList<Item> inventory)
        {
            this.story = story;
            this.inventory = inventory;
        }

        public Story getStory()
        {
            return story;
        }

        public ArrayList<Item> getInventory()
        {
            return inventory;
        }
    }

    private static class IFMLIDResolver extends IDResolver
    {
        private class BindingMap extends HashMap <String, ArrayList<Object>>
        {
            void addBinding(String name, Object object)
            {
                if(containsKey(name))
                {
                    get(name).add(object);
                }
                else
                {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    arrayList.add(object);
                    put(name, arrayList);
                }
            }

            boolean containsKeyOfClass(String name, Class aClass)
            {
                if(containsKey(name))
                {
                    for(Object object : get(name))
                    {
                        if(object != null && (object.getClass().equals(aClass) || Object.class.equals(aClass))) //todo remove Object after JAXB fix
                        {
                            return true;
                        }
                    }
                }
                return false;
            }

            Object getObjectOfClass(String name, Class aClass)
            {
                if(containsKey(name))
                {
                    for(Object object : get(name))
                    {
                        if(object != null && (object.getClass().equals(aClass) || Object.class.equals(aClass))) //todo remove Object after JAXB fix
                        {
                            return object;
                        }
                    }
                }
                return null;
            }
        }
        private final BindingMap bindings = new BindingMap();

        private Story story;

        @Override
        public void startDocument(ValidationEventHandler validationEventHandler) throws SAXException
        {
            LOG.debug("startDocument()");
            super.startDocument(validationEventHandler);
            bindings.clear();
            story = null;
        }

        @Override
        public void bind(String s, Object o) throws SAXException
        {
            LOG.debug("bind(s = \"{0}\", o = \"{1}\"); class of o is {2}", s, o,
                    o != null ? o.getClass() : "[o is null!]");

            // save link to story
            if(o instanceof Story)
            {
                LOG.debug("bind() :: parameter Object o is Story, saving: {0}", o);
                story = (Story) o;
            }
            else
            {
                bindings.addBinding(s, o);
            }
        }

        @Override
        public Callable<?> resolve(final String s, final Class aClass) throws SAXException
        {
            LOG.debug("resolve(s = \"{0}\", aClass = \"{1}\")", s, aClass);

            return new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    LOG.debug("call() :: Trying to resolve \"{0}\" for \"{1}\" class", s, aClass);

                    if(bindings.containsKeyOfClass(s, aClass))
                    {
                        Object object = bindings.getObjectOfClass(s, aClass);
                        LOG.debug("call() ::    => binding \"{0}\"; class is {1}", object,
                                object != null ? object.getClass() : "[o is null!]");
                        return object;
                    }
                    else
                    {
                        LOG.debug("call() ::   not found in bindings, trying to find in libs: ...");
                        // try to find key in libraries
                        if(story != null)
                        {
                            LOG.debug("call() ::    story is not null, trying to get story.getLibraries(). " +
                                    "getLibraries() returns {0}", story.getLibraries());
                            for (Object object : story.getLibraries())
                            {
                                LOG.debug("call() ::   test object class from getLibraries(): {0}", object.getClass());
                                if(!(object instanceof Library))
                                {
                                    throw new IFML2Exception("Member of getLibraries isn't a Library! It's {0}", object.getClass());
                                }
                                Library library = (Library)object;
                                LOG.debug("call() ::   - searching in lib {0}, class is {1}", library.getName(), aClass);

                                // attributes
                                if(aClass == Attribute.class || aClass == Object.class) //todo: remove Object after JAXB fix
                                {
                                    LOG.debug("call() ::   => searching Attribute \"{0}\"", s);
                                    Attribute attribute = library.getAttributeByName(s);
                                    if(attribute != null)
                                    {
                                        LOG.debug("call() ::     - found Attribute: \"{0}\"", attribute);
                                        return attribute;
                                    }
                                    /*todo из-за приходящего Object вместо нормального Attribute мы не сможем проверить,
                                      есть ли такой аттрибут вообще (не понятно, что запрашивает, атрибут или другой объект)
                                      */
                                }
                                // actions
                                else if(aClass == Action.class || aClass == Object.class) //todo: remove Object after JAXB fix
                                {
                                    LOG.debug("call() ::   => searching Action \"{0}\"", s);
                                    Action action = library.getActionByName(s);
                                    if(action != null)
                                    {
                                        LOG.debug("call() ::     - found Action: \"{0}\"", action);
                                        return action;
                                    }
                                }
                                else
                                {
                                    LOG.debug("call() ::     it's nor Attribute, nor Action, nothing to search else");
                                }

                                //todo ANOTHER LINKS!
                            }
                        }
                        else
                        {
                            LOG.debug("call() ::    story is null");
                        }
                    }
                    LOG.debug("call() ::   -> Binding NOT FOUND");
                    return null;
                }
            };
        }

        @Override
        public void endDocument() throws SAXException
        {
            LOG.debug("endDocument()");
            super.endDocument();
        }
    }
}
