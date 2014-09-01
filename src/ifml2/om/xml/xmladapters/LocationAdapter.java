package ifml2.om.xml.xmladapters;

import ifml2.om.ExitDirection;
import ifml2.om.Location;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocationAdapter extends XmlAdapter<LocationAdapter.AdaptedLocation, Location>
{
    @Override
    public Location unmarshal(AdaptedLocation adaptedLocation) throws Exception
    {
        return adaptedLocation;
    }

    @Override
    public AdaptedLocation marshal(Location location) throws Exception
    {
        return new AdaptedLocation(location);
    }

    @XmlAccessorType(XmlAccessType.NONE)
    @XmlType(name="location")
    public static class AdaptedLocation extends Location implements Cloneable
    {
        @Override
        /**
         * Override getExit() to yield exits from plain fields to HashMap
         */
        protected Location getExit(ExitDirection exitDirection)
        {
            switch (exitDirection)
            {
                case NORTH:
                    return north;
                case EAST:
                    return east;
                case SOUTH:
                    return south;
                case WEST:
                    return west;
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
         */
        protected void setExit(ExitDirection exitDirection, Location location)
        {
            switch (exitDirection)
            {
                case NORTH:
                    north = location;
                    break;
                case EAST:
                    east = location;
                    break;
                case SOUTH:
                    south = location;
                    break;
                case WEST:
                    west = location;
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

        @XmlElement(name = "east")
        @XmlIDREF
        private Location east;

        @XmlElement(name = "south")
        @XmlIDREF
        private Location south;

        @XmlElement(name = "west")
        @XmlIDREF
        private Location west;

        @XmlElement(name = "up")
        @XmlIDREF
        private Location up;

        @XmlElement(name = "down")
        @XmlIDREF
        private Location down;

        @SuppressWarnings("UnusedDeclaration") // needed for JAXB
        public AdaptedLocation()
        {
        }

        public AdaptedLocation(@NotNull Location location) throws CloneNotSupportedException
        {
            location.copyTo(this);
            fillFieldsFromLoc(location);
        }

        private void fillFieldsFromLoc(Location location)
        {
            north = location.getNorth();
            east = location.getEast();
            south = location.getSouth();
            west = location.getWest();
            up = location.getUp();
            down = location.getDown();
        }
    }
}
