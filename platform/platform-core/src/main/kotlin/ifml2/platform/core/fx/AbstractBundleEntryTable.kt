package ifml2.platform.core.fx

import javafx.collections.FXCollections
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleEvent
import org.osgi.util.tracker.BundleTracker
import org.osgi.util.tracker.BundleTrackerCustomizer
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

abstract class AbstractBundleEntryTable {

    internal open var logger = LoggerFactory.getLogger(AbstractBundleEntryTable::class.java)
    private val entriesBackend = ArrayList<BundleEntry>()
    val entries = FXCollections.observableList(entriesBackend)

    internal var context: BundleContext? = null
    internal var bt: BT? = null

    fun attach(context: BundleContext) {
        this.context = context
        bt = BT(context, null)
        bt!!.open()
    }

    fun detach() {
        bt!!.close()
        bt = null
        this.context = null
    }

    internal inner class BT(context: BundleContext, customizer: BundleTrackerCustomizer<Bundle>?) : BundleTracker<Bundle>(context, Bundle.UNINSTALLED or Bundle.INSTALLED or
            Bundle.RESOLVED or Bundle.STARTING or
            Bundle.STOPPING or Bundle.ACTIVE, customizer) {

        override fun addingBundle(bundle: Bundle, event: BundleEvent?): Bundle {
            val b = super.addingBundle(bundle, event)
            bundleAdded(b)
            return b
        }

        override fun modifiedBundle(bundle: Bundle, event: BundleEvent?,
                                    `object`: Bundle?) {
            super.modifiedBundle(bundle, event, `object`)
            bundleModified(bundle)
        }

        override fun removedBundle(bundle: Bundle, event: BundleEvent?,
                                   `object`: Bundle?) {
            super.removedBundle(bundle, event, `object`)
            bundleRemoved(bundle)
        }
    }

    protected fun getBundleEntry(f: File): BundleEntry? {
        for (be in entries) {
            if (f == be.file) {
                return be
            }
        }
        return null
    }

    protected fun getBundleEntry(b: Bundle): BundleEntry? {
        for (be in entries) {
            val beb: Bundle = be.bundle.value
            if (beb != null && beb.bundleId == b.bundleId) {
                return be
            }
        }
        return null
    }

    protected fun addBundleEntry(be: BundleEntry) {
        entries.add(be)
    }

    protected fun removeBundleEntry(be: BundleEntry) {
        entries.remove(be)
    }

    internal abstract fun bundleAdded(b: Bundle)

    internal abstract fun bundleRemoved(bundle: Bundle)

    internal abstract fun bundleModified(bundle: Bundle)

}
