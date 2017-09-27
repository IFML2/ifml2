package ifml2;

import ifml2.engine.Engine;
import ifml2.engine.IEngine;
import ifml2.engine.featureproviders.IPlayerFeatureProvider;

public enum ServiceRegistry {
    ;

    public static final IEngine getEngine(IPlayerFeatureProvider playerFeatureProvider) {
        return new Engine(playerFeatureProvider);
    }

}
