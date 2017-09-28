package ifml2;

import ifml2.engine.Engine;
import ifml2.engine.IEngine;
import ifml2.engine.featureproviders.PlayerFeatureProvider;

public enum ServiceRegistry {
    ;

    public static final IEngine getEngine(PlayerFeatureProvider playerFeatureProvider) {
        return new Engine(playerFeatureProvider);
    }

}
