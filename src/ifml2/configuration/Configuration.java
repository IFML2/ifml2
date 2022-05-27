package ifml2.configuration;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Class for aggregation of all configuration:
 * path to libraries, path to games directory, etc.
 */
public class Configuration {
    private static final Logger LOG = Logger.getLogger(Configuration.class);

    @NotNull
    private final LocationConfiguration locationConfiguration;

    Configuration(@NotNull LocationConfiguration locationConfiguration) {
        this.locationConfiguration = locationConfiguration;
    }

    public Path getLibsPath() {
        return locationConfiguration.getLibsPath();
    }

    public static class Factory {
        private static Configuration instance;

        private static @NotNull Configuration createConfiguration() {
            return new Configuration(LocationConfiguration.Factory.createLocationConfiguration());
        }

        public static @NotNull Configuration getInstance() {
            if (instance == null) instance = Factory.createConfiguration();
            return instance;
        }
    }
}
