package ifml2.om.xml.xmladapters;

import ifml2.om.Location;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocationAdapter extends XmlAdapter<LocationAdapter.AdaptedLocation, Location> {
    @Override
    public Location unmarshal(AdaptedLocation adaptedLocation) throws Exception {
        return adaptedLocation;
    }

    @Override
    public AdaptedLocation marshal(Location location) throws Exception {
        return new AdaptedLocation(location);
    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlType(name = "location")
    public static class AdaptedLocation extends Location implements Cloneable {
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
                    return null;
            }
        }

        @Override
        /**
         * Override setExit() to set exits to plain fields from HashMap
         */ public void setExit(ExitDirection exitDirection, Location location) {
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

        @SuppressWarnings("UnusedDeclaration") // needed for JAXB
        public AdaptedLocation() {
        }

        public AdaptedLocation(@NotNull Location location) throws CloneNotSupportedException {
            location.copyTo(this);
            fillFieldsFromLoc(location);
        }

        private void fillFieldsFromLoc(Location location) {
            north = location.getNorth();
            northEast = location.getExit(ExitDirection.NORTH_EAST);
            east = location.getEast();
            southEast = location.getExit(ExitDirection.SOUTH_EAST);
            south = location.getSouth();
            southWest = location.getExit(ExitDirection.SOUTH_WEST);
            west = location.getWest();
            northWest = location.getExit(ExitDirection.NORTH_WEST);
            up = location.getUp();
            down = location.getDown();
        }
    }
}
