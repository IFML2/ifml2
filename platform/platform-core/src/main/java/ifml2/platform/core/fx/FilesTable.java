package ifml2.platform.core.fx;

import java.util.LinkedHashMap;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesTable extends AbstractBundleEntryTable {

    Logger logger = LoggerFactory.getLogger(FilesTable.class);

    public FilesTable() {
        super();
    }

    @Override
    void bundleAdded(Bundle b) {
        // If the file was marked modified, mark it unchanged now:
//        BundleEntry be = getBundleEntry(b);
//        if (be!=null) {
//            be.setFileState(FileState.UNCHANGED);
//        }
    }

    @Override
    void bundleRemoved(Bundle bundle) {
        // Nothing to do here.
    }

    @Override
    void bundleModified(Bundle bundle) {
        BundleEntry be = getBundleEntry(bundle);
        if (be!=null) {
            be.fireBundleModified();
        }
    }

    public void updateWithWatchedFiles(LinkedHashMap<String, BundleEntry> changeSet) {
        logger.info("update...");
        for (BundleEntry wf : changeSet.values()) {
            BundleEntry be = null;
            switch (wf.getFileState()) {
            case ADDED:
                be = BundleEntry.createBundleEntry(wf.getFile());
                addBundleEntry(be);
                break;
            case REMOVED:
                be = getBundleEntry(wf.getFile());
                if (be!=null) {
                    removeBundleEntry(be);
                }
                break;
            case MODIFIED:
                be = getBundleEntry(wf.getFile());
                if (be!=null) {
                    be.setFileState(BundleEntry.FileState.MODIFIED);
                }
                break;
            case CURRENT:
                break;
            default:
            }
        }
        logger.info("update... done.");
    }

}
