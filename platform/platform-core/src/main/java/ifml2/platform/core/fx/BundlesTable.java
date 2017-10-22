package ifml2.platform.core.fx;

import javafx.application.Platform;
import org.osgi.framework.Bundle;

public class BundlesTable extends AbstractBundleEntryTable {

    public BundlesTable() {
        super();
    }

    @Override
    void bundleAdded(final Bundle b) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getEntries().add(BundleEntry.createBundleEntry(b));
            }
        });
    }

    @Override
    void bundleRemoved(final Bundle bundle) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                BundleEntry be = getBundleEntry(bundle);
                getEntries().remove(be);
            }
        });
    }

    @Override
    void bundleModified(final Bundle bundle) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                BundleEntry be = getBundleEntry(bundle);
                if (be!=null) {
                    be.fireBundleModified();
                }
            }
        });
    }

}
