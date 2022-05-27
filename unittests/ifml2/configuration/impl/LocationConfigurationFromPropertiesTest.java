package ifml2.configuration.impl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationConfigurationFromPropertiesTest {
    @Test
    public void canBeCreated_shall_return_false_for_not_existing_file() {
        //Act
        boolean canBeCreated = LocationConfigurationFromProperties.canBeCreated("really not existing file");
        //Assert
        assertFalse(canBeCreated);
    }

    @Test
    public void canBeCreated_shall_return_true_for_existing_file() throws IOException {
        //Arrange
        File tempFile = File.createTempFile("UnitTest", "LocationConfigurationFromProperties");
        tempFile.deleteOnExit();
        //Act
        boolean canBeCreated = LocationConfigurationFromProperties.canBeCreated(tempFile.getAbsolutePath());
        //Assert
        assertTrue(canBeCreated);
    }

    @Test
    public void getLibsPath() {
        //todo
    }
}