package ifml2.platform.core;

import java.util.LinkedHashMap;

import org.osgi.framework.Bundle;

public class BundleFilesTableModel extends AbstractBundlesTableModel {

    private static final long serialVersionUID = 1L;

    public BundleFilesTableModel() {
        super();
    }

    /**
     * Updates the set of entries based on the changes detected in the 
     * bundles location in the file system. (See DirectoryWatcher class)
     * 
     * @param changeSet Contains BundleEntry instances representing added,
     *                  modified or removed files.
     */
    public void updateWithWatchedFiles(LinkedHashMap<String, BundleEntry> changeSet) {
        for (BundleEntry wf : changeSet.values()) {
            BundleEntry be = null;
            switch (wf.getStatus()) {
            case ADDED:
                be = new BundleEntry(wf.getFile());
                be.addFlag(BundleEntry.FILE_ADDED);
                addBundleEntry(be);
                break;
            case REMOVED:
                be = findBundleEntry(wf);
                if (be!=null) {
                    removeBundleEntry(be);
                }
                break;
            case MODIFIED:
                be = findBundleEntry(wf);
                if (be!=null) {
                    be.addFlag(BundleEntry.FILE_MODIFIED);
                    be.fireChange();
                }
                break;
            case UNCHANGED:
                break;
            default:
            }
        }
    }

    // ////////////////////////////////////////////////////////////////
    // Methods which are called from the BundleTracker.  
    // The methods are called from within the EDT.
    // 

    protected void bundleAdded(Bundle b) {
        // Nothing to do here. 
        // The entry for the File already is added and the link
        // between File and Bundle is done in the install method (see MainUI).
    }

    protected void bundleModified(Bundle b) {
        // Find the BundleEntry for the Bundle and let observers know, 
        // that it was modified. (File entry in the table will update 
        // its display.)
        BundleEntry be = getBundleEntry(b);
        if (be==null) {
            return;
        }
        be.fireChange();
    }

    protected void bundleRemoved(Bundle b) {
        // Find the BundleEntry for the Bundle and let observers know,
        // that it was modified. (File entry in the table will update 
        // its display.)
        BundleEntry be = getBundleEntry(b);
        if (be==null) {
            return;
        }
        be.fireChange();
    }

}
