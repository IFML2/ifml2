package ifml2.engine;

import ifml2.om.Hook;

import java.util.List;

public interface HookMap {

    void add(Hook hook);

    List<Hook> get(Hook.Type type);

    int sizeByType(Hook.Type type);

}
