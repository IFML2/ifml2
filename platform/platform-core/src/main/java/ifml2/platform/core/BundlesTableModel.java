package ifml2.platform.core;

import org.osgi.framework.Bundle;

public class BundlesTableModel extends AbstractBundlesTableModel {

    private static final long serialVersionUID = 1L;

    public BundlesTableModel() {
        super();
    }

    protected void bundleAdded(Bundle b) {
        addBundleEntry(new BundleEntry(b));
    }

    protected void bundleModified(Bundle b) {
        BundleEntry be = getBundleEntry(b);
        if (be==null) return;
        be.fireChange();
    }

    protected void bundleRemoved(Bundle b) {
        BundleEntry be = getBundleEntry(b);
        if (be==null) return;
        removeBundleEntry(be);
    }

}
