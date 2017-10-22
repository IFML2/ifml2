package ifml2.platform.core.fx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class AbstractBundleEntryTable {

    Logger logger = LoggerFactory.getLogger(AbstractBundleEntryTable.class);
    private List<BundleEntry> entriesBackend = new ArrayList<>();
    private ObservableList<BundleEntry> entries = FXCollections.observableList(entriesBackend);

    BundleContext context;
    BT bt;

    public AbstractBundleEntryTable() {
        super();
    }

    public void attach(BundleContext context) {
        this.context = context;
        bt = new BT(context, null);
        bt.open();
    }

    public void detach() {
        bt.close();
        bt = null;
        this.context = null;
    }

    public ObservableList<BundleEntry> getEntries() {
        return entries;
    }

    class BT extends BundleTracker<Bundle> {
        public BT(BundleContext context, BundleTrackerCustomizer<Bundle> customizer) {
            super(
                    context, 
                    Bundle.UNINSTALLED | Bundle.INSTALLED |
                    Bundle.RESOLVED | Bundle.STARTING |
                    Bundle.STOPPING | Bundle.ACTIVE, 
                    customizer);
        }

        @Override
        public Bundle addingBundle(Bundle bundle, BundleEvent event) {
            Bundle b = super.addingBundle(bundle, event);
            bundleAdded(b);
            return b;
        }

        @Override
        public void modifiedBundle(Bundle bundle, BundleEvent event,
                Bundle object) {
            super.modifiedBundle(bundle, event, object);
            bundleModified(bundle);
        }

        @Override
        public void removedBundle(Bundle bundle, BundleEvent event,
                Bundle object) {
            super.removedBundle(bundle, event, object);
            bundleRemoved(bundle);
        }
    }

    protected BundleEntry getBundleEntry(File f) {
        for (BundleEntry be : entries) {
            if (f.equals(be.getFile())) {
                return be;
            }
        }
        return null;
    }

    protected BundleEntry getBundleEntry(Bundle b) {
        for (BundleEntry be : entries) {
            Bundle beb = be.getBundle();
            if (beb!=null && beb.getBundleId()==b.getBundleId()) {
                return be;
            }
        }
        return null;
    }

    protected void addBundleEntry(BundleEntry be) {
        entries.add(be);
    }

    protected void removeBundleEntry(BundleEntry be) {
        entries.remove(be);
    }

    abstract void bundleAdded(Bundle b);

    abstract void bundleRemoved(Bundle bundle);

    abstract void bundleModified(Bundle bundle);

}
