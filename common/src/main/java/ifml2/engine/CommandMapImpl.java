package ifml2.engine;

import java.util.HashMap;
import java.util.Map;

import static ifml2.engine.SystemCommand.HELP;
import static ifml2.engine.SystemCommand.UNKNOWN;

public class CommandMapImpl implements CommandMap {

    private final Map<String, SystemCommand> commandMap = new HashMap() {
        {
            put("помощь", HELP);
            put("помоги", HELP);
            put("помогите", HELP);
            put("инфо", HELP);
            put("информация", HELP);
            put("help", HELP);
            put("info", HELP);
        }
    };

    public SystemCommand get(String key) {
        return commandMap.getOrDefault(key, UNKNOWN);
    }

    public boolean containsKey(String key) {
        return commandMap.containsKey(key);
    }

}
