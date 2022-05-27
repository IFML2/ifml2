package ifml2.configuration.impl;

import ifml2.configuration.LocationConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LocationConfigurationDefaults extends LocationConfiguration {
    private static final Path LIBS_DEFAULT_PATH = Paths.get("libs");
    @Override
    public Path getLibsPath() {
        return LIBS_DEFAULT_PATH;
    }
}
