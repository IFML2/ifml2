package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.om.xml.xmladapters.DictionaryAdapter;
import ifml2.om.xml.xmladapters.ProceduresAdapter;
import ifml2.om.xml.xmladapters.UsedLibrariesAdapter;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.*;

@XmlRootElement(name = "story")
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

    @SuppressWarnings("FieldCanBeLocal") // todo remove suppress after JAXB bug is fixed
    @XmlAttribute(name = "id")
    @XmlID
    private String id = "story";

    @XmlElement(name = "storyOptions")
    public final StoryOptions storyOptions = new StoryOptions();

    public StoryOptions getStoryOptions()
    {
        return storyOptions;
    }

    @XmlJavaTypeAdapter(value=UsedLibrariesAdapter.class)
	private EventList<Library> libraries = new BasicEventList<Library>();
    public EventList<Library> getLibraries()
    {
        return libraries;
    }

    @XmlJavaTypeAdapter(value=DictionaryAdapter.class)
	private HashMap<String, Word> dictionary = new HashMap<String, Word>();
    public HashMap<String, Word> getDictionary()
    {
        return dictionary;
    }

    @XmlElementWrapper(name = STORY_LOCATIONS_ELEMENT)
    @XmlElement(name = LOCATIONS_LOCATION_ELEMENT)
    private EventList<Location> locations = new BasicEventList<Location>();

    // subscribe to location changes for items update
    {
        locations.addListEventListener(new ListEventListener<Location>()
        {
            @Override
            public void listChanged(ListEvent<Location> listChanges)
            {
                while(listChanges.next() && listChanges.getType() == ListEvent.DELETE)
                {
                    for(Item item : items)
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
                    /*for(Location location : locations)
                    {
                        //todo: delete links from north, south etc.
                    }*/
                }
            }
        });
    }
    public EventList<Location> getLocations() { return locations; }

    @XmlElementWrapper(name = STORY_ITEMS_ELEMENT)
    @XmlElement(name = ITEMS_ITEM_ELEMENT)
    private EventList<Item> items = new BasicEventList<Item>();
    public EventList<Item> getItems() { return items; }

    /**
     * objectsHeap holds all game object - locations and items
     */
    private HashMap<String, IFMLObject> objectsHeap = new HashMap<String, IFMLObject>(); // todo subscribe objectsHeap to locations and items updates

    public HashMap<String, IFMLObject> getObjectsHeap()
    {
        return objectsHeap;
    }

    @XmlTransient
    public void setObjectsHeap(HashMap<String, IFMLObject> objectsHeap)
    {
        this.objectsHeap = objectsHeap;
    }

    // todo subscribe all objects to attributes change
    @XmlElementWrapper(name="actions")
    @XmlElement(name="action")
    private EventList<Action> actions = new BasicEventList<Action>();
    public EventList<Action> getActions() { return actions; }

    @XmlTransient
    private EventList<Action> allActions = null;
    public EventList<Action> getAllActions()
    {
    	if(allActions == null)
    	{
	    	allActions = new BasicEventList<Action>();
	    	if(actions != null)
	    	{
	    		allActions.addAll(getActions());
	    	}
	    	if(libraries != null)
	    	{
	    		for(Library library : libraries)
	    		{
	    			allActions.addAll(library.actions);
	    		}
	    	}
    	}
    	return allActions;
	}

    @XmlTransient
    private EventList<Attribute> allAttributes = null;

    public EventList<Attribute> getAllAttributes()
    {
        if(allAttributes == null)
        {
            allAttributes = new BasicEventList<Attribute>();
            if(libraries != null)
            {
                for(Library library : libraries)
                {
                    allAttributes.addAll(library.getAttributes());
                }
            }
        }
        return allAttributes;
    }

    @XmlTransient
    private EventList<RoleDefinition> allRoleDefinitions = null;

    public List<RoleDefinition> getAllRoleDefinitions()
    {
        if(allRoleDefinitions == null)
        {
            allRoleDefinitions = new BasicEventList<RoleDefinition>();
            if(libraries != null)
            {
                for(Library library : libraries)
                {
                    allRoleDefinitions.addAll(library.getRoleDefinitions());
                }
            }
        }
        return allRoleDefinitions;
    }

    @XmlJavaTypeAdapter(value=ProceduresAdapter.class)
    private final HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();

    public HashMap<String, Procedure> getProcedures() { return procedures; }

    @Override
    public String toString()
    {
        return MessageFormat.format("История \"{0}\"", id);
    }

    public String generateIdByName(String name, @NotNull Class forClass)
    {
        if(name == null || "".equals(name))
        {
            return "";
        }

        String[] words = name.split("\\s");

        String camelCaseId = "";

        for(String word : words)
        {
            camelCaseId += CommonUtils.uppercaseFirstLetter(word);
        }

        String classedId = camelCaseId;

        // adding type postfix for more uniqueness (avoiding JAXB collection typing bug JAXB-546)
        if(Location.class.equals(forClass))
        {
            classedId += "Лок";
        }
        else if(Item.class.equals(forClass))
        {
            classedId += "Пред";
        }
        else
        {
            throw new NotImplementedException();
        }

        String id = classedId;

        int counter = 1;
        while(findObjectById(id) != null)
        {
            id = classedId + counter;
            counter++;
        }

        return id;
    }

    /**
     * Returns object by ID
     * @param id object id
     * @return object if found, null otherwise
     */
    public Object findObjectById(String id)
    {
        String loweredId = id.toLowerCase();

        if(objectsHeap.containsKey(loweredId))
        {
            return objectsHeap.get(loweredId);
        }

        if(dictionary.containsKey(loweredId))
        {
            return dictionary.get(loweredId);
        }

        if(procedures.containsKey(loweredId))
        {
            return procedures.get(loweredId);
        }

        return null;
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

    public String getObjectClassName(@NotNull Object object) throws IFML2Exception
    {
        Class objectClass = object.getClass();

        if(CLASSES_NAMES.containsKey(objectClass))
        {
            return CLASSES_NAMES.get(objectClass);
        }

        throw new IFML2Exception("Имя для класса {0} не определено в системе.", objectClass);
    }
}