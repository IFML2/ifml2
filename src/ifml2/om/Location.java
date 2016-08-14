package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.IFML2Exception;
import ifml2.vm.ISymbolResolver;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.function.Supplier;

@XmlTransient
public class Location extends IFMLObject implements Cloneable
{
    private HashMap<ExitDirection, Location> exits = new HashMap<>();
    protected EventList<Item> items = new BasicEventList<>(); // refs
    private HashMap<String, Supplier<? extends Value>> LOCATION_SYMBOLS = new HashMap<String, Supplier<? extends Value>>()
    {
        {
            for (ExitDirection exitDir : ExitDirection.values()) {
                put(exitDir.getName(), () -> getObjectValue(exitDir));
            }
            put("предметы", () -> new CollectionValue(items));
        }

        @NotNull
        private ObjectValue getObjectValue(ExitDirection exitDirection)
        {
            return new ObjectValue(getExit(exitDirection));
        }
    };

    @NotNull
    @Contract(pure = true)
    static String getClassName()
    {
        return "Локация";
    }

    @Override
    public Location clone() throws CloneNotSupportedException
    {
        Location location = (Location) super.clone(); // flat clone
        location.exits = new HashMap<>(exits); //copy refs
        location.items = GlazedLists.eventList(items); // copy refs
        return location;
    }

    public Location getExit(ExitDirection exitDirection)
    {
        return exits.get(exitDirection);
    }

    public void setExit(ExitDirection exitDirection, Location location)
    {
        exits.put(exitDirection, location);
    }

    public EventList<Item> getItems()
    {
        return items;
    }

    // is loaded in OMManager through item.location
    public void setItems(EventList<Item> items)
    {
        this.items = items;
    }

    /**
     * Checks whether location contains item.
     *
     * @param item item to check
     * @return True if location contains item and false otherwise.
     */
    public boolean contains(Item item)
    {
        return items.contains(item);
    }

    @Override
    public Value getMemberValue(@NotNull String propertyName, ISymbolResolver symbolResolver) throws IFML2Exception
    {
        String loweredPropName = propertyName.toLowerCase();

        if (LOCATION_SYMBOLS.containsKey(loweredPropName))
        {
            try
            {
                return LOCATION_SYMBOLS.get(loweredPropName).get();
            }
            catch (Exception e)
            {
                throw new IFML2Exception(e, "Ошибка при вычислении свойства \"{0}\" у локации {1}", propertyName, getId());
            }
        }
        else
        {
            return super.getMemberValue(propertyName, symbolResolver);
        }
    }

    public void copyTo(@NotNull Location location) throws CloneNotSupportedException
    {
        super.copyTo(location);
        location.exits = new HashMap<>(exits); // copy refs
        location.items = GlazedLists.eventList(items); // copy refs
    }

    /**
     * Location exit directions
     */
    public enum ExitDirection
    {
        NORTH("север"),
        NORTH_EAST("северо-восток"),
        EAST("восток"),
        SOUTH_EAST("юго-восток"),
        SOUTH("юг"),
        SOUTH_WEST("юго-запад"),
        WEST("запад"),
        NORTH_WEST("северо-запад"),
        UP("вверх"),
        DOWN("вниз");
        private final String name;

        ExitDirection(String name)
        {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
