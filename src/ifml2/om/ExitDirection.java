package ifml2.om;

/**
 * Location exit directions
 */
public enum ExitDirection
{
    NORTH("север"),
    EAST("восток"),
    SOUTH("юг"),
    WEST("запад"),
    UP("вверх"),
    DOWN("вниз");
    private final String name;

    ExitDirection(String name)
    {
        this.name = name;
    }
}
