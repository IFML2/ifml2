package ifml2.platform.core.fx

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class DirectoryWatcher(
        private val directory: File,
        private val actionListener: EventHandler<ActionEvent>
) : Runnable {

    internal var logger = LoggerFactory.getLogger(DirectoryWatcher::class.java)

    private var thread: Thread? = null
    private val entries = LinkedHashMap<String, BundleEntry>()

    init {
        startThread()
    }

    private fun startThread() {
        thread = Thread(this, "DirectoryWatcher")
        // Make the thread a daemon so it won't keep the JVM alive on
        // application exit:
        thread!!.isDaemon = true
        thread!!.start()
    }

    override fun run() {
        val changeSet = LinkedHashMap<String, BundleEntry>()
        while (thread != null && !Thread.interrupted()) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                // Lets stop in case of interruption
                break
            }

            val currentFiles = scanDirectory() ?: // The currently watched directory is gone.
                    // Don't do anything.
                    continue
            changeSet.clear()  // (be sure the changeSet is clean)
            computeChangeSet(currentFiles, changeSet)
            applyChangeSet(changeSet)

            if (changeSet.size > 0) {
                // There were changes in the directory. Create a copy of the
                // changeSet and pass it along to the actionListener.
                // Note that the actionListener shall be
                // called in the JavaFX Application Thread - the watcher thread shall not be
                // blocked by this.
                val tmpChgSet = LinkedHashMap(changeSet)
                Platform.runLater {
                    actionListener.handle(
                            ActionEvent(tmpChgSet, null))
                }
            } // (...changeSet not empty?)
            changeSet.clear() // (clean up the changeSet after it has been applied)
        } // (...while)
        thread = null
    }

    private fun applyChangeSet(changeSet: LinkedHashMap<String, BundleEntry>) {
        for (wf in changeSet.values) {
            if (wf.getFileState() == BundleEntry.FileState.REMOVED) {
                entries.remove(wf.file.name)
            } else {
                entries.put(wf.file.name, wf)
            }
        }
    }

    private fun computeChangeSet(currentFiles: Array<File>, changeSet: LinkedHashMap<String, BundleEntry>) {
        for (file in currentFiles) {
            val name = file.name
            var watchedFile: BundleEntry? = entries[name]
            if (watchedFile == null) {
                // A file with this name is not known. It must have been
                // added since the last check. So create a new entry.
                watchedFile = BundleEntry.createBundleEntry(file)
                watchedFile!!.updateFileProperties()
                watchedFile.setFileState(BundleEntry.FileState.ADDED)
                changeSet.put(name, watchedFile)
                logger.debug("File added: {}", file)
            } else {
                // A file with this name is known. Check whether it was
                // modified in the mean time.
                if (watchedFile.isModified(file)) {
                    watchedFile.updateFileProperties()
                    watchedFile.setFileState(BundleEntry.FileState.MODIFIED)
                    changeSet.put(name, watchedFile)
                    logger.debug("File modified: {}", file)
                } else {
                    // File is known and was not changed.
                }
            }
        } // (... for all files)

        val currentFilesSet = HashSet(Arrays.asList(*currentFiles))
        for (be in entries.values) {
            val f: File = be.file.value ?: continue
            if (currentFilesSet.contains(f)) continue

            val deleted = BundleEntry.createBundleEntry(f)
            deleted.setFileState(BundleEntry.FileState.REMOVED)
            changeSet.put(deleted.file.name, deleted)
            logger.debug("File removed: {}", deleted.file)
        }
    }

    private fun scanDirectory(): Array<File>? {
        return if (!directory.exists()) {
            null
        } else directory.listFiles { pathname -> if (pathname.isFile) true else false }
    }

    fun stop() {
        if (thread != null) {
            thread!!.interrupt()
            thread = null
        }
    }

}
