package ifml2.om.xml.xmladapters;

import ifml2.om.Location;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;

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

    @SuppressWarnings("unused")
    @XmlAccessorType(XmlAccessType.NONE)
    @XmlType(name = "location")
    public static class AdaptedLocation extends Location implements Cloneable {
        /**
         * Constructor needed for JAXB
         */
        public AdaptedLocation() {
        }

        public AdaptedLocation(@NotNull Location location) throws CloneNotSupportedException {
            location.copyTo(this);
        }

        @XmlElement(name = "north")
        @XmlIDREF
        private Location getNorth() {
            return getExit(NORTH);
        }

        private void setNorth(Location north) {
            setExit(NORTH, north);
        }

        @XmlElement(name = "north-east")
        @XmlIDREF
        public Location getNorthEast() {
            return getExit(NORTH_EAST);
        }

        void setNorthEast(Location northEast) {
            setExit(NORTH_EAST, northEast);
        }

        @XmlElement(name = "east")
        @XmlIDREF
        public Location getEast() {
            return getExit(EAST);
        }

        public void setEast(Location east) {
            setExit(EAST, east);
        }

        @XmlElement(name = "south-east")
        @XmlIDREF
        public Location getSouthEast() {
            return getExit(SOUTH_EAST);
        }

        public void setSouthEast(Location southEast) {
            setExit(SOUTH_EAST, southEast);
        }

        @XmlElement(name = "south")
        @XmlIDREF
        public Location getSouth() {
            return getExit(SOUTH);
        }

        public void setSouth(Location south) {
            setExit(SOUTH, south);
        }

        @XmlElement(name = "south-west")
        @XmlIDREF
        public Location getSouthWest() {
            return getExit(SOUTH_WEST);
        }

        public void setSouthWest(Location southWest) {
            setExit(SOUTH_WEST, southWest);
        }

        @XmlElement(name = "west")
        @XmlIDREF
        public Location getWest() {
            return getExit(WEST);
        }

        public void setWest(Location west) {
            setExit(WEST, west);
        }

        @XmlElement(name = "north-west")
        @XmlIDREF
        public Location getNorthWest() {
            return getExit(NORTH_WEST);
        }

        public void setNorthWest(Location northWest) {
            setExit(NORTH_WEST, northWest);
        }

        @XmlElement(name = "up")
        @XmlIDREF
        public Location getUp() {
            return getExit(UP);
        }

        public void setUp(Location up) {
            setExit(UP, up);
        }

        @XmlElement(name = "down")
        @XmlIDREF
        public Location getDown() {
            return getExit(DOWN);
        }

        public void setDown(Location down) {
            setExit(DOWN, down);
        }
    }
}
