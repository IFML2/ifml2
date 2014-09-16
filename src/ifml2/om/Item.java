package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.IFML2Exception;
import ifml2.om.xml.XmlSchemaConstants;
import ifml2.vm.VirtualMachine;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import java.util.List;

import static ifml2.om.xml.XmlSchemaConstants.ITEM_STARTING_POSITION_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.STARTING_POSITION_INVENTORY_ELEMENT;

@XmlAccessorType(XmlAccessType.NONE)
public class Item extends IFMLObject implements Cloneable
{
    @XmlElement(name = ITEM_STARTING_POSITION_ELEMENT)
    private ItemStartingPosition startingPosition = new ItemStartingPosition();
    @XmlTransient
    private List<?> container;

    public ItemStartingPosition getStartingPosition()
    {
        return startingPosition;
    }

    public EventList<Hook> getHooks()
    {
        return hooks;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * Just runs appropriate trigger
     *
     * @param virtualMachine Virtual Machine
     * @return Value returned by trigger
     */
    public Value getAccessibleContent(VirtualMachine virtualMachine) throws IFML2Exception
    {
        //todo: run own triggers -- when they will exist

        // run roles' triggers
        for (Role role : roles)
        {
            Trigger trigger = role.getRoleDefinition().getTrigger(Trigger.TriggerTypeEnum.GET_ACCESSIBLE_CONTENT);
            if (trigger != null)
            {
                return virtualMachine.runTrigger(trigger, this);
            }
        }

        return null;
    }

    public List<?> getContainer()
    {
        return container;
    }

    public void setContainer(List<?> container)
    {
        this.container = container;
    }

    public void moveTo(@NotNull List<Item> collection)
    {
        // contract
        assert container != null;
        //

        container.remove(this);
        collection.add(this);
        container = collection;
    }

    @Override
    public Item clone() throws CloneNotSupportedException
    {
        Item clone = (Item) super.clone();
        clone.startingPosition = startingPosition.clone();
        clone.hooks = GlazedLists.eventList(hooks);
        return clone;
    }

    public static String getClassName()
    {
        return "Предмет";
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ItemStartingPosition implements Cloneable
    {
        private boolean inventory = false;
        @XmlElementWrapper(name = XmlSchemaConstants.STARTING_POSITION_LOCATIONS_ELEMENT)
        @XmlElement(name = XmlSchemaConstants.STARTING_POSITION_LOCATION_ELEMENT)
        @XmlIDREF
        private EventList<Location> locations = new BasicEventList<Location>();

        @Override
        public ItemStartingPosition clone() throws CloneNotSupportedException
        {
            ItemStartingPosition clone = (ItemStartingPosition) super.clone();
            clone.locations = GlazedLists.eventList(locations);
            return clone;
        }

        public boolean getInventory()
        {
            return inventory;
        }

        @XmlElement(name = STARTING_POSITION_INVENTORY_ELEMENT)
        public void setInventory(boolean inventory)
        {
            this.inventory = inventory;
        }

        public EventList<Location> getLocations()
        {
            return locations;
        }
    }
}
