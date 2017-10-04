package ifml2.service;

import java.util.ArrayList;
import java.util.List;

import ifml2.Environment;
import ifml2.EnvironmentImpl;
import ifml2.engine.Engine;
import ifml2.engine.EngineImpl;
import ifml2.engine.featureproviders.graphic.OutputIconProvider;
import ifml2.engine.featureproviders.text.OutputPlainTextProvider;
import ifml2.parser.ParserImpl;
import ifml2.vm.VirtualMachineImpl;

public enum ServiceRegistry {
    INSTANCE;

    private List<Ifml2Service> services = new ArrayList<>();

    public void registerService(final Ifml2Service service) {
        service.start();
        services.add(service);
    }

    public void unregisterService(final Ifml2Service service) {
        service.stop();
        services.remove(service);
    }

    public Ifml2Service findService(final String name) {
        return services.stream().filter(srv -> srv.getName().equals(name)).findAny().orElse(null);
    }

    private ServiceRegistry() {
    }

    public static final Engine getEngine(
            final OutputPlainTextProvider textProvider,
            final OutputIconProvider iconProvider
    ) {
        Environment environment = new EnvironmentImpl(textProvider, iconProvider);
        return new EngineImpl(environment, new VirtualMachineImpl(environment), new ParserImpl());
    }

    public static final String VERSION = "Выпуск 2016/5";

}
