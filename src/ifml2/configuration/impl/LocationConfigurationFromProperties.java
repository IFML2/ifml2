package ifml2.configuration.impl;

import ifml2.configuration.IFML2ConfigurationException;
import ifml2.configuration.LocationConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class LocationConfigurationFromProperties extends LocationConfiguration {
    private static final Logger LOG = Logger.getLogger(LocationConfigurationFromProperties.class);
    private static File propertiesFile;
    private final String LIBS_KEY = "libs.location";
    private final Properties properties = new Properties();

    public LocationConfigurationFromProperties(String fileName) throws IFML2ConfigurationException {
        propertiesFile = new File(fileName);
        loadProperties();
    }

    public static boolean canBeCreated(String fileName) {
        File testFile = new File(fileName);
        boolean isFileExist = testFile.exists() && testFile.isFile();
        if (!isFileExist)
            LOG.warn(format("No location properties file found by path %s", testFile.getAbsolutePath()));
        return isFileExist;
    }

    private void loadProperties() throws IFML2ConfigurationException {
        String absolutePath = propertiesFile.getAbsolutePath();
        try {
            properties.load(Files.newInputStream(propertiesFile.toPath()));
            LOG.debug(format("Location properties file %s loaded", absolutePath));
            logAllProperties();
        } catch (IOException e) {
            LOG.error(format("Error while loading properties file %s", absolutePath), e);
            throw new IFML2ConfigurationException(e, format("Не удалось прочитать файл настроек путей платформы из файла %s", absolutePath));
        }
    }

    private void logAllProperties() {
        String propertiesList = properties.entrySet().stream()
                .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(System.lineSeparator()));
        LOG.debug(format("Location properties:\n%s", propertiesList));
    }

    @Override
    public Path getLibsPath() {
        return Paths.get(properties.getProperty(LIBS_KEY));
    }
}
