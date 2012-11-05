package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.om.xml.XmlSchemaConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Item extends IFMLObject implements Cloneable
{
    @XmlElement(name = ITEM_STARTING_POSITION_ELEMENT)
    public ItemStartingPosition startingPosition = new ItemStartingPosition();

    public ItemStartingPosition getStartingPosition()
    {
        return startingPosition;
    }

    @XmlElementWrapper(name = ITEM_HOOKS_ELEMENT)
    @XmlElement(name = ITEM_HOOK_ELEMENT)
    public EventList<Hook> hooks = new BasicEventList<Hook>();

    public EventList<Hook> getHooks()
    {
        return hooks;
    }

    @Override
	public String toString()
	{
		return getName();
	}

    public static class ItemStartingPosition implements Cloneable
    {
        @Override
        public ItemStartingPosition clone() throws CloneNotSupportedException
        {
            ItemStartingPosition clone = (ItemStartingPosition) super.clone();
            clone.locations = GlazedLists.eventList(locations);
            return clone;
        }

        @XmlElement(name = STARTING_POSITION_INVENTORY_ELEMENT)
        public boolean inventory = false;

        @XmlElementWrapper(name = XmlSchemaConstants.STARTING_POSITION_LOCATIONS_ELEMENT)
        @XmlElement(name = XmlSchemaConstants.STARTING_POSITION_LOCATION_ELEMENT)
        @XmlIDREF
        public EventList<Location> locations = new BasicEventList<Location>();

        public EventList<Location> getLocations()
        {
            return locations;
        }

        public boolean getInventory()
        {
            return inventory;
        }
    }

    @Override
    public Item clone() throws CloneNotSupportedException
    {
        Item clone = (Item) super.clone();
        clone.startingPosition = startingPosition.clone();
        clone.hooks = GlazedLists.eventList(hooks);
        return clone;
    }
}
