package ifml2.om.xml.xmladapters;

import ifml2.om.Location;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.function.Supplier;

import static ifml2.om.Location.ExitDirection.*;

public class LocationAdapter extends XmlAdapter<LocationAdapter.AdaptedLocation, Location> {
    @Override
    public Location unmarshal(AdaptedLocation adaptedLocation) throws Exception {
        return adaptedLocation;
    }

    @Override
    public AdaptedLocation marshal(Location location) throws Exception {
        return location instanceof AdaptedLocation ? (AdaptedLocation) location : new AdaptedLocation(location);
    }

    // TODO: 14.08.2016 try to move to get and set using super and @XmlElement above getNorth and setNorth and so on
    
    @XmlAccessorType(XmlAccessType.NONE)
    @XmlType(name = "location")
    public static class AdaptedLocation extends Location implements Cloneable {
        @XmlElement(name = "north")
        @XmlIDREF
        private Location north;
        @XmlElement(name = "north-east")
        @XmlIDREF
        private Location northEast;
        @XmlElement(name = "east")
        @XmlIDREF
        private Location east;
        @XmlElement(name = "south-east")
        @XmlIDREF
        private Location southEast;
        @XmlElement(name = "south")
        @XmlIDREF
        private Location south;
        @XmlElement(name = "south-west")
        @XmlIDREF
        private Location southWest;
        @XmlElement(name = "west")
        @XmlIDREF
        private Location west;
        @XmlElement(name = "north-west")
        @XmlIDREF
        private Location northWest;
        @XmlElement(name = "up")
        @XmlIDREF
        private Location up;
        @XmlElement(name = "down")
        @XmlIDREF
        private Location down;

        /**
         * Constructor needed for JAXB
         */
        public AdaptedLocation() {
        }

        public AdaptedLocation(@NotNull Location location) throws CloneNotSupportedException {
            location.copyTo(this);
            north = location.getExit(NORTH);
            northEast = location.getExit(NORTH_EAST);
            east = location.getExit(EAST);
            southEast = location.getExit(SOUTH_EAST);
            south = location.getExit(SOUTH);
            southWest = location.getExit(SOUTH_WEST);
            west = location.getExit(WEST);
            northWest = location.getExit(NORTH_WEST);
            up = location.getExit(UP);
            down = location.getExit(DOWN);
        }

        HashMap<ExitDirection, Supplier<Location>> exitsMap = new HashMap<ExitDirection, Supplier<Location>>() {
            {
                put(NORTH, () -> north);  // FIXME: 14.08.2016 add others
            }
        };

        /**
         * Override getExit() to yield exits from plain fields to HashMap
         */
        @Override
        public Location getExit(ExitDirection exitDirection) {
            switch (exitDirection) {
                case NORTH:
                    return north;
                case NORTH_EAST:
                    return northEast;
                case EAST:
                    return east;
                case SOUTH_EAST:
                    return southEast;
                case SOUTH:
                    return south;
                case SOUTH_WEST:
                    return southWest;
                case WEST:
                    return west;
                case NORTH_WEST:
                    return northWest;
                case UP:
                    return up;
                case DOWN:
                    return down;
                default:
                    throw new IllegalArgumentException(String.format("Unknown ExitDirection = %s", exitDirection));
            }
        }

        /**
         * Override setExit() to set exits to plain fields from HashMap
         */
        @Override
        public void setExit(ExitDirection exitDirection, Location location) {
            switch (exitDirection) {
                case NORTH:
                    north = location;
                    break;
                case NORTH_EAST:
                    northEast = location;
                    break;
                case EAST:
                    east = location;
                    break;
                case SOUTH_EAST:
                    southEast = location;
                    break;
                case SOUTH:
                    south = location;
                    break;
                case SOUTH_WEST:
                    southWest = location;
                    break;
                case WEST:
                    west = location;
                    break;
                case NORTH_WEST:
                    northWest = location;
                    break;
                case UP:
                    up = location;
                    break;
                case DOWN:
                    down = location;
                    break;
            }
        }

    }
}
