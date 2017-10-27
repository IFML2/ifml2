package ifml2.platform.core.fx

import org.osgi.framework.Bundle
import org.slf4j.LoggerFactory
import java.util.*

class FilesTable : AbstractBundleEntryTable() {

    internal override var logger = LoggerFactory.getLogger(FilesTable::class.java)

    internal override fun bundleAdded(bundle: Bundle) {
        // If the file was marked modified, mark it unchanged now:
        //        BundleEntry be = getBundleEntry(b);
        //        if (be!=null) {
        //            be.setFileState(FileState.UNCHANGED);
        //        }
    }

    internal override fun bundleRemoved(bundle: Bundle) {
        // Nothing to do here.
    }

    internal override fun bundleModified(bundle: Bundle) {
        val be = getBundleEntry(bundle)
        be?.fireBundleModified()
    }

    fun updateWithWatchedFiles(changeSet: LinkedHashMap<String, BundleEntry>) {
        logger.info("update...")
        for (wf in changeSet.values) {
            var be: BundleEntry? = null
            when (wf.getFileState()) {
                BundleEntry.FileState.ADDED -> {
                    be = BundleEntry.createBundleEntry(wf.file.value)
                    addBundleEntry(be)
                }
                BundleEntry.FileState.REMOVED -> {
                    be = getBundleEntry(wf.file.value)
                    if (be != null) {
                        removeBundleEntry(be)
                    }
                }
                BundleEntry.FileState.MODIFIED -> {
                    be = getBundleEntry(wf.file.value)
                    if (be != null) {
                        be.setFileState(BundleEntry.FileState.MODIFIED)
                    }
                }
                BundleEntry.FileState.CURRENT -> {
                }
            }
        }
        logger.info("update... done.")
    }

}
