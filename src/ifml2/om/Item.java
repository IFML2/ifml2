package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.IFML2Exception;
import ifml2.om.xml.XmlSchemaConstants;
import ifml2.vm.VirtualMachine;
import ifml2.vm.values.Value;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.*;

public class Item extends IFMLObject implements Cloneable
{
    @XmlElement(name = ITEM_STARTING_POSITION_ELEMENT)
    public ItemStartingPosition startingPosition = new ItemStartingPosition();

    @XmlTransient
    private List parent;

    public ItemStartingPosition getStartingPosition()
    {
        return startingPosition;
    }

    @XmlElementWrapper(name = ITEM_HOOKS_ELEMENT)
    @XmlElement(name = ITEM_HOOK_ELEMENT)
    public EventList<Hook> hooks = new BasicEventList<Hook>();

//    /**
//     * JAXB afterUnmarshal listener
//     * @param unmarshaller Unmarshaller
//     * @param parent Parent, should be Role
//     */
//    @SuppressWarnings("UnusedDeclaration")
//    private void afterUnmarshal(final Unmarshaller unmarshaller,
//                                final Object parent)
//    {
//        assert parent instanceof List;
//        parentLIst = (List) parent;
//    }

    @Override
	public String toString()
	{
		return getName();
	}

    /***
     * Just runs appropriate trigger
     * @return Value returned by trigger
     * @param virtualMachine Virtual Machine
     */
    public Value getAccessibleContent(VirtualMachine virtualMachine) throws IFML2Exception
    {
        //todo: run own triggers -- when they will exist

        // run roles' triggers
        for (Role role : getRoles())
        {
            Trigger trigger = role.getRoleDefinition().getTrigger(Trigger.TriggerTypeEnum.GET_ACCESSIBLE_CONTENT);
            if(trigger != null)
            {
                return virtualMachine.runTrigger(trigger, this);
            }
        }

        return null;
    }

    public void setParent(List parent)
    {
        this.parent = parent;
    }

    public List getParent()
    {
        return parent;
    }

    public void move(List collection)
    {
        assert parent != null;
        assert collection != null;
        parent.remove(this);
        collection.add(this);
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
