package ifml2;

import ifml2.engine.EngineImpl;
import ifml2.engine.Engine;
import ifml2.engine.featureproviders.PlayerFeatureProvider;

public enum ServiceRegistry {
    ;

    public static final Engine getEngine(PlayerFeatureProvider playerFeatureProvider) {
        return new EngineImpl(playerFeatureProvider);
    }

}
