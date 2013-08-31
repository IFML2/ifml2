package ifml2.om;


import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "location")
public class Location extends IFMLObject
{
    private Location north;
    private Location east;
    private Location south;
    private Location west;
    private Location up;
    private Location down;
    private List<Item> items = new ArrayList<Item>();

    public Location getNorth()
    {
        return north;
    }

    @XmlElement(name = "north")
    @XmlIDREF
    public void setNorth(Location north)
    {
        this.north = north;
    }

    public Location getEast()
    {
        return east;
    }

    @XmlElement(name = "east")
    @XmlIDREF
    public void setEast(Location east)
    {
        this.east = east;
    }

    public Location getSouth()
    {
        return south;
    }

    @XmlElement(name = "south")
    @XmlIDREF
    public void setSouth(Location south)
    {
        this.south = south;
    }

    public Location getWest()
    {
        return west;
    }

    @XmlElement(name = "west")
    @XmlIDREF
    public void setWest(Location west)
    {
        this.west = west;
    }

    public List<Item> getItems()
    {
        return items;
    }

    @XmlTransient // is loaded in OMManager through item.location
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
    public Value getMemberValue(String propertyName, RunningContext runningContext) throws IFML2Exception
    {
        if ("север".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(north);
        }
        else if ("восток".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(east);
        }
        else if ("юг".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(south);
        }
        else if ("запад".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(west);
        }
        else if ("верх".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(up);
        }
        else if ("низ".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(down);
        }
        else if ("предметы".equalsIgnoreCase(propertyName))
        {
            return new CollectionValue(items);
        }
        else
        {
            return super.getMemberValue(propertyName, runningContext);
        }
    }

    @XmlElement(name = "up")
    @XmlIDREF
    public void setUp(Location up)
    {
        this.up = up;
    }

    @XmlElement(name = "down")
    @XmlIDREF
    public void setDown(Location down)
    {
        this.down = down;
    }
}
