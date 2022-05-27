package ifml2.configuration;

import ifml2.configuration.impl.LocationConfigurationDefaults;
import ifml2.configuration.impl.LocationConfigurationFromProperties;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Configuration of IFML location paths
 */
public abstract class LocationConfiguration {
    private static final Logger LOG = Logger.getLogger(LocationConfiguration.class);

    public abstract Path getLibsPath();

    public static class Factory {
        private static final String LOCATION_PROPERTIES_FILENAME = "locations.properties";
        public static @NotNull LocationConfiguration createLocationConfiguration() {
            if (LocationConfigurationFromProperties.canBeCreated(LOCATION_PROPERTIES_FILENAME)) {
                try {
                    return new LocationConfigurationFromProperties(LOCATION_PROPERTIES_FILENAME);
                } catch (IFML2ConfigurationException e) {
                    LOG.warn(format("Error during creation of location properties class %s", LocationConfigurationFromProperties.class.getSimpleName()));
                    return new LocationConfigurationDefaults();
                }
            }
            return new LocationConfigurationDefaults();
        }
    }
}
