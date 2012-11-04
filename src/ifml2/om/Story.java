package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ifml2.CommonUtils;
import ifml2.om.xml.xmladapters.DictionaryAdapter;
import ifml2.om.xml.xmladapters.ProceduresAdapter;
import ifml2.om.xml.xmladapters.UsedLibrariesAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.*;

@XmlRootElement(name = "story")
public class Story
{
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
	private final HashMap<String, Word> dictionary = new HashMap<String, Word>();
    public HashMap<String, Word> getDictionary()
    {
        return dictionary;
    }

    @XmlElementWrapper(name = STORY_LOCATIONS_ELEMENT)
    @XmlElement(name = LOCATIONS_LOCATION_ELEMENT)
    private final EventList<Location> locations = new BasicEventList<Location>();

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
                    for(Location location : locations)
                    {
                        //todo: delete links from north, south etc.
                    }
                }
            }
        });
    }
    public EventList<Location> getLocations() { return locations; }

    @XmlElementWrapper(name = STORY_ITEMS_ELEMENT)
    @XmlElement(name = ITEMS_ITEM_ELEMENT)
    private final EventList<Item> items = new BasicEventList<Item>();
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
    private final EventList<Action> actions = new BasicEventList<Action>();
    public EventList<Action> getActions() { return actions; }

    @XmlTransient
    private List<Action> allActions = null;
    public List<Action> getAllActions()
    {
    	if(allActions == null)
    	{
	    	allActions = new ArrayList<Action>();
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

    @XmlJavaTypeAdapter(value=ProceduresAdapter.class)
    private final HashMap<String, Procedure> procedures = new HashMap<String, Procedure>();

    public HashMap<String, Procedure> getProcedures() { return procedures; }

    @Override
    public String toString()
    {
        return MessageFormat.format("История:\n" +
                "  Локаций: {0}\n" +
                "  Предметов: {1}\n", locations.size(), items.size());
    }

    public String generateIdByName(String name)
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

        String id = camelCaseId;

        int counter = 1;
        while(objectsHeap.containsKey(id.toLowerCase())
                || dictionary.containsKey(id.toLowerCase())
                || getProcedures().containsKey(id.toLowerCase()))
        {
            id = camelCaseId + counter;
            counter++;
        }

        return id;
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
}