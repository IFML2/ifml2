package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.FormatLogger;
import ifml2.IFML2Exception;
import ifml2.IFMLEntity;
import ifml2.om.xml.XmlSchemaConstants;
import ifml2.vm.IFML2VMException;
import ifml2.vm.SymbolResolver;
import ifml2.vm.VirtualMachine;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

import static ifml2.om.Trigger.Type.GET_ACCESSIBLE_CONTENT;
import static ifml2.om.xml.XmlSchemaConstants.ITEM_STARTING_POSITION_ELEMENT;
import static ifml2.om.xml.XmlSchemaConstants.STARTING_POSITION_INVENTORY_ELEMENT;
import static java.lang.String.format;

@XmlAccessorType(XmlAccessType.NONE)
public class Item extends IFMLObject implements Cloneable {
    private static final FormatLogger LOG = FormatLogger.getLogger(Item.class);
    private static final String CONTAINER_PROP_NAME = "СодержащаяКоллекция";
    @XmlElement(name = ITEM_STARTING_POSITION_ELEMENT)
    private ItemStartingPosition startingPosition = new ItemStartingPosition();

    @XmlTransient
    private List<? extends IFMLEntity> container;

    @NotNull
    @Contract(pure = true)
    static String getClassName() {
        return "Предмет";
    }

    public ItemStartingPosition getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(ItemStartingPosition startingPosition) {
        this.startingPosition = startingPosition;
    }

    /**
     * Copies all field of item but container.
     *
     * @param item item to copy to
     * @throws CloneNotSupportedException
     */
    public void copyTo(@NotNull Item item) throws CloneNotSupportedException {
        super.copyTo(item);
        item.setStartingPosition(startingPosition.clone());
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Just runs appropriate trigger
     *
     * @param virtualMachine Virtual Machine
     * @return Value returned by trigger
     */
    public CollectionValue getAccessibleContent(VirtualMachine virtualMachine) throws IFML2Exception {
        //todo: run own triggers -- when they will exist

        List<IFMLEntity> allAccessibleObjects = new ArrayList<>();

        // run roles' triggers
        for (Role role : roles) {
            Trigger trigger = role.getRoleDefinition().getTrigger(GET_ACCESSIBLE_CONTENT);
            if (trigger == null) {
                continue;
            }

            Value<?> value = virtualMachine.runTrigger(trigger, this);
            if (value == null) {
                continue;
            }

            if (value instanceof CollectionValue) {
                List<? extends IFMLEntity> accessibleObjects = ((CollectionValue) value).getValue();
                allAccessibleObjects.addAll(accessibleObjects);
            } else {
                throw new IFML2VMException(format("Триггер доступного содержимого у роли \"%s\" предмета \"%s\" вернул не коллекцию, а \"%s\"!", role.getName(), this, value));
            }
        }

        return new CollectionValue(allAccessibleObjects);
    }

    public List<? extends IFMLEntity> getContainer() {
        return container;
    }

    public void setContainer(List<? extends IFMLEntity> container) {
        this.container = container;
    }

    public void moveTo(@NotNull List<Item> collection) {
        // contract
        assert container != null;
        //

        if (!container.contains(this)) {
            LOG.error("moveTo(): Item's container hasn't the item!");
        }

        container.remove(this);
        collection.add(this);
        container = collection;
    }

    @Override
    public Item clone() throws CloneNotSupportedException {
        Item clone = (Item) super.clone(); // flat clone

        // deep clone
        clone.startingPosition = startingPosition.clone();

        return clone;
    }

    @Override
    public Value getMemberValue(@NotNull String propertyName, @NotNull SymbolResolver symbolResolver) throws IFML2Exception {
        if (CONTAINER_PROP_NAME.equalsIgnoreCase(propertyName)) return new CollectionValue(container);
        else
            return super.getMemberValue(propertyName, symbolResolver);
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ItemStartingPosition implements Cloneable {
        @XmlElement(name = STARTING_POSITION_INVENTORY_ELEMENT)
        private boolean inventory = false;

        @XmlElementWrapper(name = XmlSchemaConstants.STARTING_POSITION_LOCATIONS_ELEMENT)
        @XmlElement(name = XmlSchemaConstants.STARTING_POSITION_LOCATION_ELEMENT)
        @XmlIDREF
        private EventList<Location> locations = new BasicEventList<Location>();

        @Override
        public ItemStartingPosition clone() throws CloneNotSupportedException {
            ItemStartingPosition clone = (ItemStartingPosition) super.clone(); // flat clone

            // deep clone
            clone.locations = GlazedLists.eventList(locations); // copy refs

            return clone;
        }

        public boolean getInventory() {
            return inventory;
        }

        public void setInventory(boolean inventory) {
            this.inventory = inventory;
        }

        public EventList<Location> getLocations() {
            return locations;
        }
    }
}
