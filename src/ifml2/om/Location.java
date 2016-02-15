package ifml2.om;

import ifml2.IFML2Exception;
import ifml2.vm.ISymbolResolver;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

@XmlTransient
public class Location extends IFMLObject implements Cloneable
{
    protected HashMap<ExitDirection, Location> exits = new HashMap<ExitDirection, Location>();
    protected List<Item> items = new ArrayList<Item>();
    private HashMap<String, Callable<? extends Value>> LOCATION_SYMBOLS = new HashMap<String, Callable<? extends Value>>()
    {
        {
            put("север", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.NORTH);
                }
            });
            put("северовосток", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.NORTH_EAST);
                }
            });
            put("восток", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.EAST);
                }
            });
            put("юговосток", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.SOUTH_EAST);
                }
            });
            put("юг", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.SOUTH);
                }
            });
            put("югозапад", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.SOUTH_WEST);
                }
            });
            put("запад", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.WEST);
                }
            });
            put("северозапад", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.NORTH_WEST);
                }
            });
            put("верх", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.UP);
                }
            });
            put("низ", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return getObjectValue(ExitDirection.DOWN);
                }
            });

            put("предметы", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return new CollectionValue(items);
                }
            });
        }

        @NotNull
        private ObjectValue getObjectValue(ExitDirection exitDirection)
        {
            return new ObjectValue(getExit(exitDirection));
        }
    };

    public static String getClassName()
    {
        return "Локация";
    }

    @Override
    public Location clone() throws CloneNotSupportedException
    {
        Location location = (Location) super.clone();
        copyFieldsTo(location);
        return location;
    }

    private void copyFieldsTo(Location location)
    {
        location.exits = new HashMap<ExitDirection, Location>(exits);
        location.items = new ArrayList<Item>(items);
    }

    public Location getExit(ExitDirection exitDirection)
    {
        return exits.get(exitDirection);
    }

    public void setExit(ExitDirection exitDirection, Location location)
    {
        exits.put(exitDirection, location);
    }

    public Location getNorth()
    {
        return getExit(ExitDirection.NORTH);
    }

    public void setNorth(Location north)
    {
        setExit(ExitDirection.NORTH, north);
    }

    public Location getEast()
    {
        return getExit(ExitDirection.EAST);
    }

    public void setEast(Location east)
    {
        setExit(ExitDirection.EAST, east);
    }

    public Location getSouth()
    {
        return getExit(ExitDirection.SOUTH);
    }

    public void setSouth(Location south)
    {
        setExit(ExitDirection.SOUTH, south);
    }

    public Location getWest()
    {
        return getExit(ExitDirection.WEST);
    }

    public void setWest(Location west)
    {
        setExit(ExitDirection.WEST, west);
    }

    public List<Item> getItems()
    {
        return items;
    }

    //@XmlTransient // is loaded in OMManager through item.location
    public void setItems(List<Item> items)
    {
        this.items = items;
    }

    /**
     * Checks whether location contains item.
     *
     * @param item item to check
     * @return True if location contains item and false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
                return LOCATION_SYMBOLS.get(loweredPropName).call();
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

    public Location getDown()
    {
        return getExit(ExitDirection.DOWN);
    }

    public void setDown(Location down)
    {
        setExit(ExitDirection.DOWN, down);
    }

    public Location getUp()
    {
        return getExit(ExitDirection.UP);
    }

    public void setUp(Location up)
    {
        setExit(ExitDirection.UP, up);
    }

    public void copyTo(@NotNull Location location) throws CloneNotSupportedException
    {
        super.copyTo(location);
        copyFieldsTo(location);
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
    }
}
