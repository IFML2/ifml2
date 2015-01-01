package ifml2.om;

/**
 * Location exit directions
 */
public enum ExitDirection
{
    NORTH("север"),
    NORTH_EAST("северо-восток"),
    EAST("восток"),
    SOUTH_EAST("юго-восток"),
    SOUTH("юг"),
    SOUTH_WEST("юго-запад"),
    WEST("запад"),
    NORTH_WEST("северо-запад"),
    UP("вверх"),
    DOWN("вниз");
    private final String name;

    ExitDirection(String name)
    {
        this.name = name;
    }
}
