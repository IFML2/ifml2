package ifml2.platform.core.fx

import javafx.application.Platform
import org.osgi.framework.Bundle

class BundlesTable : AbstractBundleEntryTable() {

    internal override fun bundleAdded(bundle: Bundle) {
        Platform.runLater { entries.add(BundleEntry.createBundleEntry(bundle)) }
    }

    internal override fun bundleRemoved(bundle: Bundle) {
        Platform.runLater {
            val be = getBundleEntry(bundle)
            entries.remove(be)
        }
    }

    internal override fun bundleModified(bundle: Bundle) {
        Platform.runLater {
            val be = getBundleEntry(bundle)
            be?.fireBundleModified()
        }
    }

}
