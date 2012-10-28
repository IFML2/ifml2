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

@XmlRootElement(name="location")
public class Location extends IFMLObject 
{
	private Location north;
	@XmlElement(name = "north")
	@XmlIDREF
	public Location getNorth() { return north; }
	public void setNorth(Location north) { this.north = north; }

	@XmlElement(name = "east")
	@XmlIDREF
	public Location east;

	@XmlElement(name = "south")
	@XmlIDREF
	public Location south;

	@XmlElement(name = "west")
	@XmlIDREF
	public Location west;

    @XmlElement(name = "up")
    @XmlIDREF
    private Location up;

    @XmlElement(name = "down")
    @XmlIDREF
    private Location down;

    private List<Item> items = new ArrayList<Item>();
    @XmlTransient // is loaded in OMManager through item.location
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    /**
     * Checks whether location contains item.
     * @param item item to check
     * @return True if location contains item and false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean contains(Item item)
	{
		return items.contains(item);
	}

    @Override
    public Value getPropertyValue(String propertyName, RunningContext runningContext) throws IFML2Exception
    {
        if ("север".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(getNorth());
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
        else if("верх".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(up);
        }
        else if("низ".equalsIgnoreCase(propertyName))
        {
            return new ObjectValue(down);
        }
        else if("предметы".equalsIgnoreCase(propertyName))
        {
            return new CollectionValue(items);
        }
        else
        {
            return super.getPropertyValue(propertyName, runningContext);
        }
    }
}
