package ifml2.om.xml.xmladapters;

import ifml2.om.ExitDirection;
import ifml2.om.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocationAdapter extends XmlAdapter<LocationAdapter.AdaptedLocation, Location>
{
    @Override
    public Location unmarshal(AdaptedLocation v) throws Exception
    {
        return v.clone();
    }

    @Override
    public AdaptedLocation marshal(Location v) throws Exception
    {
        return new AdaptedLocation(v); //todo check that location is fully copied
    }

    //@XmlAccessorType(XmlAccessType.NONE)
    public static class AdaptedLocation extends Location implements Cloneable
    {
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

        @Override
        public Location clone() throws CloneNotSupportedException
        {
            fillExitsList();
            return super.clone();
        }

        private void fillExitsList()
        {
            exits.clear();
            setExit(ExitDirection.NORTH, north);
            setExit(ExitDirection.EAST, east);
            setExit(ExitDirection.SOUTH, south);
            setExit(ExitDirection.WEST, west);
            setExit(ExitDirection.UP, up);
            setExit(ExitDirection.DOWN, down);
        }

        private void setExit(@NotNull ExitDirection direction, @Nullable Location location)
        {
            if (location != null)
            {
                exits.put(direction, location);
            }
        }

        public AdaptedLocation()
        {
        }

        public AdaptedLocation(@NotNull Location location) throws CloneNotSupportedException
        {
            location.copyTo(this);
        }
    }
}
