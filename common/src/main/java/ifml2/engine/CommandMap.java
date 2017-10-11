package ifml2.engine;

public interface CommandMap {

    SystemCommand get(String key);

    boolean containsKey(String key);

}
