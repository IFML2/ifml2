package ifml2.platform.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBundlesTableModel extends AbstractTableModel implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    protected Logger logger = LoggerFactory.getLogger(AbstractBundlesTableModel.class);
    protected BundleContext context;
    private List<BundleEntry> bundleEntries = new ArrayList<>();

    private BundleTracker<Bundle> bundleTracker;

    static String[] columnNames = {
            "Details",
            "State",
        };

    public AbstractBundlesTableModel() {
        super();
    }

    @Override
    public int getRowCount() {
        return bundleEntries.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BundleEntry be = bundleEntries.get(rowIndex);
        return be;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return BundleEntry.class;
    }

    protected BundleEntry findBundleEntry(BundleEntry wf) {
        for (BundleEntry be : bundleEntries) {
            Bundle b = be.getBundle();
            if (b==null) continue;
            String url = b.getLocation();
            String name = wf.getFile().getName();
            if (url.endsWith(name)) {
                return be;
            }
        }
        return null;
    }

    protected BundleEntry getBundleEntry(Bundle b) {
        for (BundleEntry be : bundleEntries) {
            if (be.getBundle()==b) {
                return be;
            }
        }
        return null;
    }


    protected void addBundleEntry(BundleEntry be) {
        int row = bundleEntries.size();
        bundleEntries.add(be);
        be.addPropertyChangeListener(this);
        fireTableRowsInserted(row, row);
    }

    protected void removeBundleEntry(BundleEntry be) {
        be.removePropertyChangeListener(this);
        bundleEntries.remove(be);
    }

    protected void clearBundleEntries() {
        for (BundleEntry be : bundleEntries) {
            be.removePropertyChangeListener(this);
        }
        bundleEntries.clear();
    }

    protected List<BundleEntry> getBundleEntries() {
        return Collections.unmodifiableList(bundleEntries);
    }

    protected void removeAllBundleEntries(List<BundleEntry> removes) {
        for (BundleEntry be : removes) {
            be.removePropertyChangeListener(this);
        }
        bundleEntries.removeAll(removes);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        BundleEntry be = (BundleEntry) evt.getSource();
        int row = bundleEntries.indexOf(be);
        fireTableRowsUpdated(row, row);
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
            final Bundle b = super.addingBundle(bundle, event);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    bundleAdded(b);
                }
            });
            return b;
        }

        @Override
        public void modifiedBundle(final Bundle bundle, BundleEvent event,
                Bundle object) {
            super.modifiedBundle(bundle, event, object);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    bundleModified(bundle);
                }
            });
        }

        @Override
        public void removedBundle(final Bundle bundle, BundleEvent event,
                Bundle object) {
            super.removedBundle(bundle, event, object);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    bundleRemoved(bundle);
                }
            });
        }
        
    }

    public void attach(BundleContext context) {
        this.context = context;
        bundleTracker = new BT(context, null);
        bundleTracker.open();
    }

    public void detach() {
        bundleTracker.close();
        bundleTracker = null;
        clearBundleEntries();
        fireTableDataChanged();
        this.context = null;
   }

    abstract protected void bundleAdded(Bundle b);

    abstract protected void bundleModified(Bundle b);

    abstract protected void bundleRemoved(Bundle b);

}
