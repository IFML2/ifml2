package ifml2.configuration.impl;

import ifml2.configuration.IFML2ConfigurationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LocationConfigurationFromPropertiesTest {
    @Test
    public void whenFileExists_thenCreationShallBeSuccessful() throws IOException, IFML2ConfigurationException {
        //Arrange
        File tempFile = File.createTempFile("UnitTest", "LocationConfigurationFromProperties");
        tempFile.deleteOnExit();
        //Act
        LocationConfigurationFromProperties locationConfigurationFromProperties = new LocationConfigurationFromProperties(tempFile.getAbsolutePath());
        //Assert
        assertNotNull(locationConfigurationFromProperties);
    }

    @Test(expected = IFML2ConfigurationException.class)
    public void whenFileDoesntExist_thenExceptionShallBeThrown() throws IFML2ConfigurationException {
        //Act
        new LocationConfigurationFromProperties("really not existing file");
        //Assert
        Assert.fail("Shall not reach this line since an exception shall be thrown");
    }

    @Test
    public void whenPropertiesFileExists_thenLibsPathShallBeReturnedFromIt() throws IOException, IFML2ConfigurationException {
        //Arrange
        File tempFile = File.createTempFile("UnitTest", "LocationConfigurationFromProperties");
        tempFile.deleteOnExit();
        Properties properties = new Properties();
        final String libsLocation = "my random libs location";
        properties.setProperty("libs.location", libsLocation);
        properties.store(new FileWriter(tempFile), "location properties");
        //Act
        LocationConfigurationFromProperties locationConfigurationFromProperties = new LocationConfigurationFromProperties(tempFile.getAbsolutePath());
        Path libsPath = locationConfigurationFromProperties.getLibsPath();
        //Assert
        assertEquals(libsLocation, libsPath.toString());
    }
}