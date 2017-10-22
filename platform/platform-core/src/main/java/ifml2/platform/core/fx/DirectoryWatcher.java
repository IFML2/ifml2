package ifml2.platform.core.fx;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher implements Runnable {

    Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);
    final private File directory;
    final private EventHandler<ActionEvent> actionListener;

    private Thread thread;
    private LinkedHashMap<String, BundleEntry> entries = new LinkedHashMap<>();

    /**
     * Initializes the DirectoryWatcher with the given directory and the 
     * given EventHandler. The watcher thread is created and started in this
     * method. The actionListener may already be called before this method 
     * returns.
     * 
     * @param directory     A File for the directory to watch for changes.
     * @param actionListener    An EventHandler to be called with detected changes.
     */
    public DirectoryWatcher(File directory, EventHandler<ActionEvent> actionListener) {
        super();
        this.directory = directory;
        this.actionListener = actionListener;
        startThread();
    }

    private void startThread() {
        thread = new Thread(this, "DirectoryWatcher");
        // Make the thread a daemon so it won't keep the JVM alive on 
        // application exit:
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        final LinkedHashMap<String, BundleEntry> changeSet = new LinkedHashMap<>();
        while (thread!=null && !Thread.interrupted()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Lets stop in case of interruption
                break;
            }
            File[] currentFiles = scanDirectory();
            if (currentFiles==null) {
                // The currently watched directory is gone.
                // Don't do anything.
                continue;
            }
            changeSet.clear();  // (be sure the changeSet is clean)
            computeChangeSet(currentFiles, changeSet);
            applyChangeSet(changeSet);

            if (changeSet.size()>0) {
                // There were changes in the directory. Create a copy of the 
                // changeSet and pass it along to the actionListener. 
                // Note that the actionListener shall be
                // called in the JavaFX Application Thread - the watcher thread shall not be 
                // blocked by this.
                final LinkedHashMap<String, BundleEntry> tmpChgSet = new LinkedHashMap<>(changeSet);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        actionListener.handle(
                                new ActionEvent(tmpChgSet, null));
                    }
                });
            } // (...changeSet not empty?)
            changeSet.clear(); // (clean up the changeSet after it has been applied)
        } // (...while)
        thread = null;
    }

    /**
     * Updates the current local list of files with the detected changes.
     * The next check thus works on the updated data.
     * 
     * @param changeSet The set of changes detected with computeChangeSet.
     */
    private void applyChangeSet(LinkedHashMap<String, BundleEntry> changeSet) {
        for (BundleEntry wf : changeSet.values()) {
            if (wf.getFileState()==BundleEntry.FileState.REMOVED) {
                entries.remove(wf.getFile().getName());
            } else {
                entries.put(wf.getFile().getName(), wf);
            }
        }
    }

    /**
     * Determines the changes between the fresh list of files and the current 
     * (previously stored) set of file entries in this DirectoryWatcher.
     * 
     * @param currentFiles  An array of Files read freshly from file system.
     * @param changeSet     The changeSet to be populated by this method.
     */
    private void computeChangeSet(File[] currentFiles, LinkedHashMap<String, BundleEntry> changeSet) {
        for (File file : currentFiles) {
            String name = file.getName();
            BundleEntry watchedFile = entries.get(name);
            if (watchedFile==null) {
                // A file with this name is not known. It must have been
                // added since the last check. So create a new entry.
                watchedFile = BundleEntry.createBundleEntry(file);
                watchedFile.updateFileProperties();
                watchedFile.setFileState(BundleEntry.FileState.ADDED);
                changeSet.put(name, watchedFile);
                logger.debug("File added: {}", file);
            } else {
                // A file with this name is known. Check whether it was
                // modified in the mean time.
                if (watchedFile.isModified(file)) {
                    watchedFile.updateFileProperties();
                    watchedFile.setFileState(BundleEntry.FileState.MODIFIED);
                    changeSet.put(name, watchedFile);
                    logger.debug("File modified: {}", file);
                } else {
                    // File is known and was not changed.
                }
            }
        } // (... for all files)

        // Check whether files in the entries are not in the list of current
        // files on filesystem. These files must have gone and will be marked
        // as deleted.
        HashSet<File> currentFilesSet = new HashSet<>(Arrays.asList(currentFiles));
        for (BundleEntry be : entries.values()) {
            File f = be.getFile();
            if (f==null) continue;
            if (currentFilesSet.contains(f)) continue;

            BundleEntry deleted = BundleEntry.createBundleEntry(f);
            deleted.setFileState(BundleEntry.FileState.REMOVED);
            changeSet.put(deleted.getFile().getName(), deleted);
            logger.debug("File removed: {}", deleted.getFile());
        }
    }

    /**
     * Reads list of Files from the watched directory. If the directory does
     * not exist (any more?), null is returned.
     * 
     * @return  Returns an array of Files read from the watched directory; 
     *          or null, if the directory does not exist.
     */
    private File[] scanDirectory() {
        if (!directory.exists()) {
            return null;
        }
        File [] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) return true;
                return false;
            }
        });
        return files;
    }

    /**
     * Signals the thread to stop.
     */
    public void stop() {
        if (thread!=null) {
            thread.interrupt();
            thread = null;
        }
    }

}
