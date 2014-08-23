package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.om.xml.xmladapters.DictionaryAdapter;
import ifml2.om.xml.xmladapters.LocationAdapter;
import ifml2.om.xml.xmladapters.ProceduresAdapter;
import ifml2.om.xml.xmladapters.UsedLibrariesAdapter;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.*;

@XmlRootElement(name = "story")
//@XmlAccessorType(XmlAccessType.NONE)
public class Story
{
    @XmlTransient
    private static HashMap<Class, String> CLASSES_NAMES = new HashMap<Class, String>()
    {
        {
            put(Location.class, Location.getClassName());
            put(Item.class, Item.getClassName());
            put(Word.class, Word.getClassName());
        }
    };
    @XmlElement(name = "storyOptions")
    private final StoryOptions storyOptions = new StoryOptions();
    @XmlJavaTypeAdapter(value = ProceduresAdapter.class)
    private final HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();
    private Story.DataHelper dataHelper = new DataHelper();
    @SuppressWarnings("FieldCanBeLocal") // todo remove suppress after JAXB bug is fixed
    @XmlAttribute(name = "id")
    @XmlID
    private String id = "story";
    @XmlJavaTypeAdapter(value = UsedLibrariesAdapter.class)
    private EventList<Library> libraries = new BasicEventList<Library>();
    @XmlJavaTypeAdapter(value = DictionaryAdapter.class)
    private HashMap<String, Word> dictionary = new HashMap<String, Word>();
    @XmlElementWrapper(name = STORY_LOCATIONS_ELEMENT)
    @XmlElement(name = LOCATIONS_LOCATION_ELEMENT)
    @XmlJavaTypeAdapter(value = LocationAdapter.class)
    //@XmlJavaTypeAdapter(value = LocationsAdapter.class)
    private EventList<Location> locations = new BasicEventList<Location>();

    // subscribe to location changes for object tree update
    {
        locations.addListEventListener(new ListEventListener<Location>()
        {
            @Override
            public void listChanged(ListEvent<Location> listChanges)
            {
                while (listChanges.next() && listChanges.getType() == ListEvent.DELETE)
                {
                    // delete from items
                    for (Item item : items)
                    {
                        EventList<Location> startLocations = item.getStartingPosition().getLocations();
                        for (Location startLocation : startLocations)
                        {
                            if (!locations.contains(startLocation))
                            {
                                startLocations.remove(startLocation);
                            }
                        }
                    }

                    // delete from locations
                    for(Location location : locations)
                    {
                        if(!locations.contains(location.getNorth()))
                        {
                            location.setNorth(null);
                        }
                        if(!locations.contains(location.getEast()))
                        {
                            location.setEast(null);
                        }
                        if(!locations.contains(location.getSouth()))
                        {
                            location.setSouth(null);
                        }
                        if(!locations.contains(location.getWest()))
                        {
                            location.setWest(null);
                        }
                        if(!locations.contains(location.getUp()))
                        {
                            location.setUp(null);
                        }
                        if(!locations.contains(location.getDown()))
                        {
                            location.setDown(null);
                        }
                        //todo: delete links from semi-directions (NE, NW, ...)
                    }

                    // delete from heap
                    for (Iterator<IFMLObject> iterator = objectsHeap.values().iterator(); iterator.hasNext(); )
                    {
                        IFMLObject object = iterator.next();
                        if (object instanceof Location && !locations.contains(object))
                        {
                            iterator.remove();
                        }
                    }
                }
            }
        });
    }

    @XmlElementWrapper(name = STORY_ITEMS_ELEMENT)
    @XmlElement(name = ITEMS_ITEM_ELEMENT)
    private EventList<Item> items = new BasicEventList<Item>();
    /**
     * objectsHeap holds all game object - locations and items
     */
    private HashMap<String, IFMLObject> objectsHeap = new HashMap<String, IFMLObject>(); // todo subscribe objectsHeap to locations and items updates
    // todo subscribe all objects to attributes change
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    private EventList<Action> actions = new BasicEventList<Action>();
    @XmlTransient
    private EventList<Action> allActions = null;
    @XmlTransient
    private EventList<Attribute> allAttributes = null;
    @XmlTransient
    private EventList<RoleDefinition> allRoleDefinitions = null;

    public DataHelper getDataHelper()
    {
        return dataHelper;
    }

    @Override
    public Story clone() throws CloneNotSupportedException
    {
        //noinspection UnnecessaryLocalVariable
        Story clone = (Story) super.clone(); // todo check subscriptions made in anonymous constructor
       /* clone.actions = GlazedLists.eventList(actions);
        clone.dictionary = new HashMap<String, Word>(dictionary);
        clone.items = GlazedLists.eventList(items);
        clone.libraries = GlazedLists.eventList(libraries);
        clone.locations = GlazedLists.eventList(locations);
        clone.objectsHeap = new HashMap<String, IFMLObject>(objectsHeap);*/
        //todo FULL COPY of objects! they all are OWN! that's why they should be copied not just links
        return clone;
    }

    public StoryOptions getStoryOptions()
    {
        return storyOptions;
    }

    public EventList<Library> getLibraries()
    {
        return libraries;
    }

    public HashMap<String, Word> getDictionary()
    {
        return dictionary;
    }

    public EventList<Location> getLocations()
    {
        return locations;
    }

    public EventList<Item> getItems()
    {
        return items;
    }

    public HashMap<String, IFMLObject> getObjectsHeap()
    {
        return objectsHeap;
    }

    @XmlTransient
    public void setObjectsHeap(HashMap<String, IFMLObject> objectsHeap)
    {
        this.objectsHeap = objectsHeap;
    }

    public EventList<Action> getActions()
    {
        return actions;
    }

    public EventList<Action> getAllActions()
    {
        if (allActions == null)
        {
            allActions = new BasicEventList<Action>();
            if (actions != null)
            {
                allActions.addAll(actions);
            }
            if (libraries != null)
            {
                for (Library library : libraries)
                {
                    allActions.addAll(library.actions);
                }
            }
        }
        return allActions;
    }

    public EventList<Attribute> getAllAttributes()
    {
        if (allAttributes == null)
        {
            allAttributes = new BasicEventList<Attribute>();
            if (libraries != null)
            {
                for (Library library : libraries)
                {
                    allAttributes.addAll(library.getAttributes());
                }
            }
        }
        return allAttributes;
    }

    public List<RoleDefinition> getAllRoleDefinitions()
    {
        if (allRoleDefinitions == null)
        {
            allRoleDefinitions = new BasicEventList<RoleDefinition>();
            if (libraries != null)
            {
                for (Library library : libraries)
                {
                    allRoleDefinitions.addAll(library.getRoleDefinitions());
                }
            }
        }
        return allRoleDefinitions;
    }

    public HashMap<String, Procedure> getProcedures()
    {
        return procedures;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("История \"{0}\"", id);
    }

    public Procedure getSystemInheritorProcedure(Procedure.SystemProcedureEnum systemProcedure)
    {
        // for this story
        for (Procedure procedure : getProcedures().values())
        {
            if (procedure.getInheritsSystemProcedure() == systemProcedure)
            {
                return procedure;
            }
        }

        // for libs
        for (Library library : libraries)
        {
            for (Procedure procedure : library.procedures.values())
            {
                if (procedure.getInheritsSystemProcedure() == systemProcedure)
                {
                    return procedure;
                }
            }
        }

        return null;
    }

    public Procedure getStartProcedure()
    {
        return storyOptions.getStartProcedureOption().getProcedure();
    }

    public Location getStartLocation()
    {
        return storyOptions.getStartLocationOption().getLocation();
    }

    public Location getAnyLocation()
    {
        return locations.iterator().next();
    }

    public boolean IsShowStartLocDesc()
    {
        return storyOptions.getStartLocationOption().getShowStartLocDesc();
    }

    public void addLocation(@NotNull Location location)
    {
        locations.add(location);
        objectsHeap.put(location.getId().toLowerCase(), location);
    }

    public void addItem(@NotNull Item item)
    {
        items.add(item);
        objectsHeap.put(item.getId().toLowerCase(), item);
    }

    public class DataHelper
    {
        public EventList<Location> getLocations()
        {
            return locations;
        }

        /**
         * Tries to find location by id. Id shouldn't be null. If finds returns it, else returns null.
         * @param id Location id.
         * @return Location if finds and null otherwise.
         */
        public Location findLocationById(@NotNull String id)
        {
            String loweredId = id.trim().toLowerCase();

            if (objectsHeap.containsKey(loweredId))
            {
                IFMLObject object = objectsHeap.get(loweredId);
                if (object instanceof Location)
                {
                    return (Location) object;
                }
            }

            return null;
        }

        public EventList<Action> getAllActions()
        {
            return Story.this.getAllActions();
        }

        public HashMap<String, Word> getDictionary()
        {
            return dictionary;
        }

        public String generateIdByName(String name, @NotNull Class forClass)
        {
            if (name == null || "".equals(name))
            {
                return "";
            }

            String[] words = name.split("\\s");

            String camelCaseId = "";

            for (String word : words)
            {
                camelCaseId += CommonUtils.uppercaseFirstLetter(word);
            }

            String classedId = camelCaseId;

            // adding type postfix for more uniqueness (avoiding JAXB collection typing bug JAXB-546)
            if (Location.class.equals(forClass))
            {
                classedId += "Лок";
            }
            else if (Item.class.equals(forClass))
            {
                classedId += "Пред";
            }
            else
            {
                throw new NotImplementedException();
            }

            String id = classedId;

            int counter = 1;
            while (findObjectById(id) != null)
            {
                id = classedId + counter;
                counter++;
            }

            return id;
        }

        public String getObjectClassName(@NotNull Object object) throws IFML2Exception
        {
            Class objectClass = object.getClass();

            if (CLASSES_NAMES.containsKey(objectClass))
            {
                return CLASSES_NAMES.get(objectClass);
            }

            throw new IFML2Exception("Имя для класса {0} не определено в системе.", objectClass);
        }

        /**
         * Returns object by ID
         *
         * @param id object id
         * @return object if found, null otherwise
         */
        public Object findObjectById(String id)
        {
            String loweredId = id.toLowerCase();

            if (objectsHeap.containsKey(loweredId))
            {
                return objectsHeap.get(loweredId);
            }

            if (dictionary.containsKey(loweredId))
            {
                return dictionary.get(loweredId);
            }

            if (procedures.containsKey(loweredId))
            {
                return procedures.get(loweredId);
            }

            return null;
        }

        public EventList<Library> getLibraries()
        {
            return Story.this.getLibraries();
        }

        public EventList<Action> getActions()
        {
            return actions;
        }

        public HashMap<String, Procedure> getProcedures()
        {
            return procedures;
        }

        public EventList<Item> getItems()
        {
            return items;
        }

        /**
         * Tries to find item by id. Id shouldn't be null. If finds returns it, else returns null.
         * @param id Item id.
         * @return Item if finds and null otherwise.
         */
        public Item findItemById(@NotNull String id)
        {
            String loweredId = id.trim().toLowerCase();

            if (objectsHeap.containsKey(loweredId))
            {
                IFMLObject object = objectsHeap.get(loweredId);
                if (object instanceof Item)
                {
                    return (Item) object;
                }
            }

            return null;
        }

        /**
         * Searches libraries list for the library by its path.
         * @param libraries Libraries list.
         * @param library Library to find by path.
         * @return true if the same library is found and false otherwise.
         */
        public boolean isLibListContainsLib(List<Library> libraries, Library library)
        {
            for (Library lib : libraries)
            {
                if (lib.getPath().equalsIgnoreCase(library.getPath()))
                {
                    return true;
                }
            }

            return false;
        }
    }
}