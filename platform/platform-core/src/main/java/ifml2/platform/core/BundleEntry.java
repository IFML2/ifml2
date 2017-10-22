package ifml2.platform.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

public class BundleEntry {

    // Identifying state for a BundleEntry:
    // (used in hashCode and equals)
    private File file;
    private Bundle bundle;
    private long bundleId;

    // Transient states:
    // (not used in hashCode and equals)
    public static enum Status {
        UNCHANGED,
        ADDED,
        REMOVED,
        MODIFIED,
    }
    Status status;
    // Track file size and modification time to detect change of file:
    long fileSize = -1;
    long fileTime = -1;

    // Used to mark modification states:
    private int flags = 0;
    public final static int NONE = 0;
    public final static int FILE_ADDED          = 1<< 1;
    public final static int FILE_REMOVED        = 1<< 2;
    public final static int FILE_MODIFIED       = 1<< 3;
    public final static int BUNDLE_STATE_MODIFIED = 1<< 4;

    // Hold an exception if one was thrown during a bundle operation:
    private Exception bundleException;

    // Allow listeners to observe changes of the instances:
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public final static String PROP_ANY = "any";

    // Used to display Bundle states user friendly:
    static Map<Integer, String> bundleStateNames = new HashMap<>();
    static {
        initMaps();
    }

    /**
     * Initializes the BundleEntry with the given Bundle. This constructor
     * should be used for BundleEntrys which represent Bundles primarily.
     * (They also can represent bundle files.)
     * 
     * @param bundle    The Bundle which this BundleEntry wraps.
     */
    public BundleEntry(Bundle bundle) {
        setBundle(bundle);
    }

    /**
     * Initializes the BundleEntry with the given File. This constructor
     * should be used for BundleEntrys which represent bundle files primarily.
     * (They also can represent Bundles.) Note that a File related instance
     * can later be "connected" to a Bundle instance.
     * 
     * @param file      The File which this BundleEntry wraps.
     */
    public BundleEntry(File file) {
        setFile(file);
    }

    /**
     * Store user friendly names for Bundle states in a map.
     */
    private static void initMaps() {
        bundleStateNames.put(Bundle.INSTALLED, "INSTALLED");
        bundleStateNames.put(Bundle.RESOLVED, "RESOLVED");
        bundleStateNames.put(Bundle.ACTIVE, "ACTIVE");
        bundleStateNames.put(Bundle.STARTING, "STARTING");
        bundleStateNames.put(Bundle.STOPPING, "STOPPING");
        bundleStateNames.put(Bundle.UNINSTALLED, "UNINSTALLED");
    }

    public File getFile() {
        return file;
    }

    public long getBundleId() {
        return bundleId;
    }

    /**
     * Sets the file property and fires PropertyChangeEvents.
     * @param file  The File to set.
     */
    public void setFile(File file) {
        Object oldValue = this.file;
        this.file = file;
        Object newValue = this.file;
        pcs.firePropertyChange(PROP_ANY, oldValue, newValue);
    }

    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Sets the bundle property, updates the bundleId property 
     * and fires PropertyChangeEvents.
     * @param bundle The Bundle to set.
     */
    public void setBundle(Bundle bundle) {
        Object oldValue = this.bundle;
        this.bundle = bundle;
        if (this.bundle!=null) {
            this.bundleId = this.bundle.getBundleId();
        } else {
            this.bundleId = -1;
        }
        Object newValue = this.bundle;
        pcs.firePropertyChange(PROP_ANY, oldValue, newValue);
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status property and fires PropertyChangeEvents.
     * @param status    The Status to set.
     */
    public void setStatus(Status status) {
        Object oldValue = this.status;
        this.status = status;
        Object newValue = this.status;
        pcs.firePropertyChange(PROP_ANY, oldValue, newValue);
    }

    /**
     * Compares the cached values for file length and file time with the ones 
     * of the given File instance. If length or time differ, return true; 
     * return false otherwise.
     * 
     * @param other The File instance to compare against.
     * @return  Returns true, if this instance's File and the given File differ.
     */
    public boolean isModified(File other) {
        if (other.length()!=this.fileSize) {
            return true;
        }
        if (other.lastModified()!=this.fileTime) {
            return true;
        }
        return false;
    }

    /**
     * Reread and cache the underlying file's length and time.
     */
    public void updateFileProperties() {
        fileSize = file.length();
        fileTime = file.lastModified();
        pcs.firePropertyChange(PROP_ANY, 0, 1);
    }

    public String getState() {
        if (bundle==null) {
            return "---";
        }
        int state = bundle.getState();
        return bundleStateNames.get(state);
    }

    /**
     * Sets the bundleException property and fires PropertyChangeEvents.
     * @param e The Exception to set.
     */
    public void setBundleException(Exception e) {
        Object oldValue = this.bundleException;
        this.bundleException = e;
        Object newValue = this.bundleException;
        pcs.firePropertyChange(PROP_ANY, oldValue, newValue);
    }

    public Exception getBundleException() {
        return bundleException;
    }

    /**
     * Combines the current flags with the given flags (binary OR operation)
     * and fires PropertyChangeEvents.
     * @param fl    The new flags to be combined.
     */
    public void addFlag(int fl) {
        int oldValue = this.flags;
        this.flags |= fl;
        int newValue = this.flags;
        pcs.firePropertyChange(PROP_ANY, oldValue, newValue);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (bundleId ^ (bundleId >>> 32));
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass()) {
            // Have a special equals for comparison with File:
            if (obj instanceof File) {
                if (this.file==null) 
                    return false;
                return this.file.equals(obj);
            }
            return false;
        }
        BundleEntry other = (BundleEntry) obj;
        if (bundleId != other.bundleId)
            return false;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        return true;
    }

    // //////////////////////////////////////////////////////////////
    // PropertyChangeSupport delegates:

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * General helper method to let observers know, that this instance
     * changed.
     * (Currently used, when the wrapped Bundle instance changes, which
     * won't be detected by the BundleEntry and its observers easily.)
     */
    public void fireChange() {
        pcs.firePropertyChange(PROP_ANY, 0, 1);
    }


    /**
     * A helper method for displaying BundleEntrys.
     * @return  Returns main information either about a bundle or about a file.
     */
    public String getMainInfoString() {
        if (file==null) {
            return 
                    "<html>" + 
                    "ID#" + Long.toString(bundleId) + " / " + 
                    bundle.getSymbolicName() + "<br>" + 
                    bundle.getLocation() + "<br>" + 
                    ((bundleException!=null) ? bundleException.getMessage() : "---") +
                    "</html>";
        } else {
            return file.getName();
        }
    }

}
