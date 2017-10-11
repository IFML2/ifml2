package ifml2.engine;

import ifml2.om.Hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HookMapImpl implements HookMap {

    private Map<Hook.Type, List<Hook>> hookMap = new HashMap() {
        {
            put(Hook.Type.BEFORE, new ArrayList<>());
            put(Hook.Type.INSTEAD, new ArrayList<>());
            put(Hook.Type.AFTER, new ArrayList<>());
        }
    };

    @Override
    public void add(Hook hook) {
        hookMap.get(hook.getType()).add(hook);
    }

    @Override
    public List<Hook> get(Hook.Type type) {
        return hookMap.get(type);
    }

    @Override
    public int sizeByType(Hook.Type type) {
        return hookMap.get(type).size();
    }

}
