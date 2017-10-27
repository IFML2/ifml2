package ifml2.platform.core.fx

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.osgi.framework.Bundle
import org.slf4j.LoggerFactory
import java.io.File

class BundleEntry {
    internal var logger = LoggerFactory.getLogger(BundleEntry::class.java)
    // A Bundle is not Property aware, so changes to a Bundle's
    // state are not automatically propagated. Extra code exists
    // in this project to detect such changes. The following two
    // properties are updated then and UI components, which are
    // bound to them will be updated automatically.
    private val bundleDetails = SimpleStringProperty(this, "bundleDetails")
    private val bundleState = SimpleObjectProperty(BundleState.UNKNOWN)

    // Identifying state for a BundleEntry:
    // (used in hashCode and equals)
    val file = SimpleObjectProperty<File>(this, "file")
    val bundle = SimpleObjectProperty<Bundle>(this, "bundle")
    var bundleId: Long = -1
        private set
    internal var fileState = SimpleObjectProperty(FileState.CURRENT)
    // Track file size and modification time to detect change of file:
    internal var fileSize: Long = -1
    internal var fileTime: Long = -1

    internal var bundleException: Exception? = null

    val bundleDetailsString: String
        get() {
            val sb = StringBuilder()
            val b = bundle.get()
            if (b == null) {
                sb.append("<No Bundle>\n<No ID> / <No Location>")
            } else {
                sb.append(bundle.get().symbolicName)
                sb.append("\n")
                sb.append("ID#")
                sb.append(java.lang.Long.toString(b.bundleId))
                sb.append(" / ")
                sb.append(b.location)
            }
            sb.append("\n")
            val e = bundleException
            if (e == null) {
                sb.append("--")
            } else {
                sb.append(e.message)
            }
            sb.trimToSize()
            return sb.toString()
        }

    // Transient states:
    // (not used in hashCode and equals)
    enum class BundleState private constructor(internal val code: Int) {
        UNKNOWN(0),
        UNINSTALLED(Bundle.UNINSTALLED),
        INSTALLED(Bundle.INSTALLED),
        RESOLVED(Bundle.RESOLVED),
        STARTING(Bundle.STARTING),
        STOPPING(Bundle.STOPPING),
        ACTIVE(Bundle.ACTIVE);


        companion object {
            internal fun forCode(code: Int): BundleState {
                for (bst in BundleState.values()) {
                    if (bst.code == code)
                        return bst
                }
                return UNKNOWN
            }
        }
    }

    enum class FileState {
        CURRENT,
        ADDED,
        REMOVED,
        MODIFIED
    }

    fun bundleDetailsProperty(): SimpleStringProperty {
        return bundleDetails
    }

    fun bundleStateProperty(): SimpleObjectProperty<BundleState> {
        return bundleState
    }

    fun fileProperty(): SimpleObjectProperty<File> {
        return file
    }

    fun fileStateProperty(): SimpleObjectProperty<FileState> {
        return fileState
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (bundleId xor bundleId.ushr(32)).toInt()
        result = prime * result + if (file.get() == null) 0 else file.get().hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as BundleEntry?
        if (bundleId != other!!.bundleId)
            return false
        if (file == null) {
            if (other.file != null)
                return false
            // Eclipse generates file.equals(other.file), but we want the
            // variant below, because file.equals() may run into infinite
            // recursion when the file property's bean value is set to this
            // BundleEntry instance. (This changes in future JavaFX versions)
        } else if (file.get() != other.file.get())
            return false
        return true
    }

    fun getFile(): File {
        return file.get()
    }

    fun setFile(file: File) {
        this.file.set(file)
    }

    fun getBundle(): Bundle {
        return bundle.get()
    }

    fun setBundle(bundle: Bundle) {
        this.bundle.set(bundle)
        this.bundleId = bundle.bundleId
        updateBundleDetails()
    }

    private fun updateBundleDetails() {
        this.bundleDetails.set(bundleDetailsString)
        this.bundleState.set(BundleState.forCode(this.bundle.get().state))
    }

    /**
     * If at some point it is known, that the Bundle referenced by this
     * BundleEntry changed, use this method to update the Property fields, which
     * this BundleEntry contains. That way observers like a TableView will
     * get notified by these properties and update its display.
     */
    fun fireBundleModified() {
        updateBundleDetails()
    }

    fun getFileState(): FileState {
        return fileState.get()
    }

    fun setFileState(state: FileState) {
        this.fileState.set(state)
    }

    fun updateFileProperties() {
        fileSize = file.get().length()
        fileTime = file.get().lastModified()
    }

    fun isModified(other: File): Boolean {
        if (other.length() != this.fileSize) {
            return true
        }
        return if (other.lastModified() != this.fileTime) {
            true
        } else false
    }

    fun getBundleException(): Exception? {
        return bundleException
    }

    fun setBundleException(bundleException: Exception) {
        this.bundleException = bundleException
        updateBundleDetails()
    }

    companion object {

        fun createBundleEntry(bundle: Bundle): BundleEntry {
            val be = BundleEntry()
            be.setBundle(bundle)
            return be
        }

        fun createBundleEntry(file: File): BundleEntry {
            val be = BundleEntry()
            be.setFile(file)
            return be
        }
    }

}
