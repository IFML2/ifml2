package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.om.Location.ExitDirection;
import ifml2.om.Procedure.SystemProcedureType;
import ifml2.om.xml.xmladapters.DictionaryAdapter;
import ifml2.om.xml.xmladapters.LocationAdapter;
import ifml2.om.xml.xmladapters.UsedLibrariesAdapter;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.MessageFormat;
import java.util.*;

import static ifml2.om.xml.XmlSchemaConstants.*;

@XmlRootElement(name = "story")
@XmlAccessorType(XmlAccessType.NONE)
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

    @XmlElement(name = "inheritedSystemProcedures")
    private final InheritedSystemProcedures inheritedSystemProcedures = new InheritedSystemProcedures();

    @XmlElement(name = "storyOptions")
    private final StoryOptions storyOptions = new StoryOptions();

    @XmlElementWrapper(name = STORY_PROCEDURES_ELEMENT)
    @XmlElement(name = PROCEDURES_PROCEDURE_ELEMENT)
    private final EventList<Procedure> procedures = new BasicEventList<>();

    private DataHelper dataHelper = new DataHelper();
    @SuppressWarnings("FieldCanBeLocal") // todo remove suppress after JAXB bug is fixed
    @XmlAttribute(name = "id")
    @XmlID
    private String id = "story";

    @XmlJavaTypeAdapter(value = UsedLibrariesAdapter.class)
    private EventList<Library> libraries = new BasicEventList<>();

    @XmlJavaTypeAdapter(value = DictionaryAdapter.class)
    private HashMap<String, Word> dictionary = new HashMap<>();

    @XmlElementWrapper(name = STORY_LOCATIONS_ELEMENT)
    @XmlElement(name = LOCATIONS_LOCATION_ELEMENT)
    @XmlJavaTypeAdapter(value = LocationAdapter.class)
    private EventList<Location> locations = new BasicEventList<>();
    @XmlElementWrapper(name = STORY_ITEMS_ELEMENT)
    @XmlElement(name = ITEMS_ITEM_ELEMENT)
    private EventList<Item> items = new BasicEventList<>();
    /**
     * objectsHeap holds all game object - locations and items
     */
    private HashMap<String, IFMLObject> objectsHeap = new HashMap<>(); // todo subscribe objectsHeap to locations and items updates
    // todo subscribe all objects to attributes change
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    private EventList<Action> actions = new BasicEventList<>();
    @XmlTransient
    private EventList<Attribute> allAttributes = null;
    @XmlTransient
    private EventList<RoleDefinition> allRoleDefinitions = null;

    {
        // subscribe to locations changes for object tree update
        locations.addListEventListener(listChanges -> {
            while (listChanges.next() && listChanges.getType() == ListEvent.DELETE)
            {
                EventList<Location> locList = listChanges.getSourceList();

                // delete from items
                for (Item item : items)
                {
                    EventList<Location> startLocations = item.getStartingPosition().getLocations();
                    startLocations.stream().filter(startLocation -> !locList.contains(startLocation)).forEach(startLocations::remove);
                }

                // delete from locations
                for (Location location : locList)
                {
                    for (ExitDirection direction : ExitDirection.values())
                    {
                        Location destination = location.getExit(direction);
                        if (destination != null && !locList.contains(destination))
                        {
                            location.setExit(direction, null);
                        }
                    }
                }

                // todo delete from instructions - procedures, hooks and so on...

                // delete from heap
                Iterator<IFMLObject> iterator = objectsHeap.values().iterator();
                while (iterator.hasNext())
                {
                    IFMLObject object = iterator.next();
                    if (object instanceof Location)
                    {
                        Location location = (Location) object;
                        if (!locList.contains(location))
                        {
                            iterator.remove();
                        }
                    }
                }
            }
        });

        // subscribe to procedures changes for object tree update
        procedures.addListEventListener(listChanges -> {
            while (listChanges.next() && listChanges.getType() == ListEvent.DELETE)
            {
                EventList<Procedure> proceduresList = listChanges.getSourceList();

                // delete from actions
                for (Action action : actions)
                {
                    Action.ProcedureCall procedureCall = action.getProcedureCall();
                    Procedure procedure = procedureCall.getProcedure();
                    if (procedure != null && !proceduresList.contains(procedure))
                    {
                        procedureCall.setProcedure(null);
                    }
                }

                // delete from start procedure
                StoryOptions.StartProcedureOption startProcedureOption = storyOptions.getStartProcedureOption();
                Procedure procedure = startProcedureOption.getProcedure();
                if (procedure != null && !proceduresList.contains(procedure))
                {
                    startProcedureOption.setProcedure(null);
                }

                // -- delete from system inherited procedures --
                Procedure parseErrorHandler = inheritedSystemProcedures.getParseErrorHandler();
                if(parseErrorHandler != null && !proceduresList.contains(parseErrorHandler))
                {
                    inheritedSystemProcedures.setParseErrorHandler(null);
                }
            }
        });

        // todo subscribe to other lists changes
    }

    public DataHelper getDataHelper()
    {
        return dataHelper;
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
    void setObjectsHeap(HashMap<String, IFMLObject> objectsHeap)
    {
        this.objectsHeap = objectsHeap;
    }

    public EventList<Action> getActions()
    {
        return actions;
    }

    public EventList<Action> getAllActions()
    {
        EventList<Action> allActions = new BasicEventList<>();
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
        return allActions;
    }

    public EventList<Attribute> getAllAttributes()
    {
        if (allAttributes == null)
        {
            allAttributes = new BasicEventList<>();
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
            allRoleDefinitions = new BasicEventList<>();
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

    public EventList<Procedure> getProcedures()
    {
        return procedures;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("История \"{0}\"", id);
    }

    public Procedure getSystemInheritorProcedure(SystemProcedureType systemProcedureType)
    {
        // for this story
        for (Procedure procedure : procedures)
        {
            if (systemProcedureType.equals(procedure.getInheritsSystemProcedure()))
            {
                return procedure;
            }
        }

        // for libs
        for (Library library : libraries)
        {
            for (Procedure procedure : library.procedures)
            {
                if (systemProcedureType.equals(procedure.getInheritsSystemProcedure()))
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

    public InheritedSystemProcedures getInheritedSystemProcedures()
    {
        return inheritedSystemProcedures;
    }

    public class DataHelper
    {
        public EventList<Location> getLocations()
        {
            return locations;
        }

        /**
         * Tries to find location by id. Id shouldn't be null. If finds returns it, else returns null.
         *
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

            return findProcedureById(loweredId);
        }

        public Procedure findProcedureById(@NotNull String name)
        {
            for (Procedure procedure : procedures)
            {
                if (name.equalsIgnoreCase(procedure.getName()))
                {
                    return procedure;
                }
            }
            return null;
        }

        public EventList<Library> getLibraries()
        {
            return libraries;
        }

        public EventList<Action> getActions()
        {
            return actions;
        }

        public EventList<Procedure> getProcedures()
        {
            return procedures;
        }

        public EventList<Item> getItems()
        {
            return items;
        }

        /**
         * Tries to find item by id. Id shouldn't be null. If finds returns it, else returns null.
         *
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
         *
         * @param libraries Libraries list.
         * @param library   Library to find by path.
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

        /**
         * Find actions where procedure is called
         *
         * @param procedure procedure for search
         * @return list of affected actions
         */
        @NotNull
        public ArrayList<Action> findActionsByProcedure(@NotNull Procedure procedure)
        {
            ArrayList<Action> results = new ArrayList<>();

            for (Action action : actions)
            {
                Action.ProcedureCall procedureCall = action.getProcedureCall();
                if (procedure.equals(procedureCall.getProcedure()))
                {
                    results.add(action);
                }
            }
            return results;
        }

        /**
         * Returns all role definitions in story and libraries. List is copied to prevent modification.
         *
         * @return copied list of all role definitions
         */
        public List<RoleDefinition> getCopyOfAllRoleDefinitions()
        {
            return GlazedLists.eventList(Story.this.getAllRoleDefinitions()); // clone list to prevent deleting
        }

        public Collection<IFMLObject> getCopyOfAllObjects()
        {
            return GlazedLists.eventList(objectsHeap.values());
        }
    }
}