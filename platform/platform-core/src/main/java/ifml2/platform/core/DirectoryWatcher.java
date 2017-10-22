package ifml2.platform.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher implements Runnable {

    Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);
    final private File directory;
    final private ActionListener actionListener;

    private Thread thread;
    private LinkedHashMap<String, BundleEntry> entries = new LinkedHashMap<>();

    /**
     * Initializes the DirectoryWatcher with the given directory and the 
     * given ActionListener. The watcher thread is created and started in this
     * method. The actionListener may already be called before this method 
     * returns.
     * 
     * @param directory     A File for the directory to watch for changes.
     * @param actionListener    An ActionListener to be called with detected changes.
     */
    public DirectoryWatcher(File directory, ActionListener actionListener) {
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
                // called in the EDT - the watcher thread shall not be 
                // blocked by this.
                final LinkedHashMap<String, BundleEntry> tmpChgSet = new LinkedHashMap<>(changeSet);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        actionListener.actionPerformed(
                                new ActionEvent(tmpChgSet, ActionEvent.ACTION_FIRST, null));
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
            if (wf.getStatus()==BundleEntry.Status.REMOVED) {
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
                watchedFile = new BundleEntry(file);
                watchedFile.updateFileProperties();
                watchedFile.setStatus(BundleEntry.Status.ADDED);
                changeSet.put(name, watchedFile);
                logger.debug("File added: {}", file);
            } else {
                // A file with this name is known. Check whether it was
                // modified in the mean time.
                if (watchedFile.isModified(file)) {
                    watchedFile.updateFileProperties();
                    watchedFile.setStatus(BundleEntry.Status.MODIFIED);
                    changeSet.put(name, watchedFile);
                    logger.debug("File modified: {}", file);
                } else {
                    // File is known and was not changed.
                }
            }
        } // (... for all files)

        // Now check whether the currently stored list of files contains
        // entries which are not in the freshly loaded list of files.
        // This means, the files have been deleted on the filesystem.
        List<BundleEntry> filesGone = new ArrayList<>(entries.values());
        // Note:
        // This operation uses a feature of BundleEntry.equals() which allows 
        // comparison to File instances:
        filesGone.removeAll(Arrays.asList(currentFiles));
        // All entries which are still here, are gone on the file system 
        // and should be treated as deleted.
        for (BundleEntry watchedFile : filesGone) {
            BundleEntry deleted = new BundleEntry(watchedFile.getFile());
            deleted.setStatus(BundleEntry.Status.REMOVED);
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
