package ifml2.configuration;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConfigurationTest {

    private Configuration configuration;
    private LocationConfiguration locationConfigurationMock;

    @Before
    public void setUp() {
        locationConfigurationMock = mock(LocationConfiguration.class);
        configuration = new Configuration(locationConfigurationMock);
    }

    @Test
    public void getLibsPath_shall_call_LocationConfiguration_getLibsPath() {
        configuration.getLibsPath();
        verify(locationConfigurationMock).getLibsPath();
    }
}