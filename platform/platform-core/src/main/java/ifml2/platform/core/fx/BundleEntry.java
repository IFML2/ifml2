package ifml2.platform.core.fx;

import java.io.File;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleEntry {
    Logger logger = LoggerFactory.getLogger(BundleEntry.class);
    // A Bundle is not Property aware, so changes to a Bundle's 
    // state are not automatically propagated. Extra code exists
    // in this project to detect such changes. The following two
    // properties are updated then and UI components, which are
    // bound to them will be updated automatically.
    private SimpleStringProperty bundleDetails = new SimpleStringProperty(this, "bundleDetails");
    private SimpleObjectProperty<BundleState> bundleState = new SimpleObjectProperty<BundleState>(BundleState.UNKNOWN);

    // Identifying state for a BundleEntry:
    // (used in hashCode and equals)
    private SimpleObjectProperty<File> file = new SimpleObjectProperty<>(this, "file");
    private SimpleObjectProperty<Bundle> bundle = new SimpleObjectProperty<>(this, "bundle");
    private long bundleId = -1;

    // Transient states:
    // (not used in hashCode and equals)
    public static enum BundleState {
        UNKNOWN(0),
        UNINSTALLED(Bundle.UNINSTALLED),
        INSTALLED(Bundle.INSTALLED),
        RESOLVED(Bundle.RESOLVED), 
        STARTING(Bundle.STARTING), 
        STOPPING(Bundle.STOPPING), 
        ACTIVE(Bundle.ACTIVE),
        ;
        final int code;
        BundleState(int code) {
            this.code = code;
        }
        static BundleState forCode(int code) {
            for (BundleState bst : BundleState.values()) {
                if (bst.code==code)
                    return bst;
            }
            return UNKNOWN;
        }
    }

    public static enum FileState {
        CURRENT,
        ADDED,
        REMOVED,
        MODIFIED,
    }
    SimpleObjectProperty<FileState> fileState = new SimpleObjectProperty<FileState>(FileState.CURRENT);
    // Track file size and modification time to detect change of file:
    long fileSize = -1;
    long fileTime = -1;

    Exception bundleException;

    public static BundleEntry createBundleEntry(Bundle bundle) {
        BundleEntry be = new BundleEntry();
        be.setBundle(bundle);
        return be;
    }

    public static BundleEntry createBundleEntry(File file) {
        BundleEntry be = new BundleEntry();
        be.setFile(file);
        return be;
    }

    public BundleEntry() {
        super();
    }

    public SimpleStringProperty bundleDetailsProperty() {
        return bundleDetails;
    }

    public SimpleObjectProperty<BundleState> bundleStateProperty() {
        return bundleState;
    }

    public SimpleObjectProperty<File> fileProperty() {
        return file;
    }

    public SimpleObjectProperty<FileState> fileStateProperty() {
        return fileState;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (bundleId ^ (bundleId >>> 32));
        result = prime * result + ((file.get() == null) ? 0 : file.get().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass()) {
            return false;
        }
        BundleEntry other = (BundleEntry) obj;
        if (bundleId != other.bundleId)
            return false;
        if (file == null) {
            if (other.file != null)
                return false;
        // Eclipse generates file.equals(other.file), but we want the
        // variant below, because file.equals() may run into infinite 
        // recursion when the file property's bean value is set to this
        // BundleEntry instance. (This changes in future JavaFX versions)
        } else if (!file.get().equals(other.file.get()))
            return false;
        return true;
    }

    public File getFile() {
        return file.get();
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public Bundle getBundle() {
        return bundle.get();
    }

    public void setBundle(Bundle bundle) {
        this.bundle.set(bundle);
        this.bundleId = bundle.getBundleId();
        updateBundleDetails();
    }

    private void updateBundleDetails() {
        this.bundleDetails.set(getBundleDetailsString());
        this.bundleState.set(BundleState.forCode(this.bundle.get().getState()));
    }

    public long getBundleId() {
        return bundleId;
    }

    /**
     * If at some point it is known, that the Bundle referenced by this
     * BundleEntry changed, use this method to update the Property fields, which
     * this BundleEntry contains. That way observers like a TableView will 
     * get notified by these properties and update its display.
     */
    public void fireBundleModified() {
        updateBundleDetails();
    }

    public FileState getFileState() {
        return fileState.get();
    }

    public void setFileState(FileState state) {
        this.fileState.set(state);
    }

    public void updateFileProperties() {
        fileSize = file.get().length();
        fileTime = file.get().lastModified();
    }

    public boolean isModified(File other) {
        if (other.length()!=this.fileSize) {
            return true;
        }
        if (other.lastModified()!=this.fileTime) {
            return true;
        }
        return false;
    }

    public Exception getBundleException() {
        return bundleException;
    }

    public void setBundleException(Exception bundleException) {
        this.bundleException = bundleException;
        updateBundleDetails();
    }

    public String getBundleDetailsString() {
        StringBuilder sb = new StringBuilder();
        Bundle b = bundle.get();
        if (b==null) {
            sb.append("<No Bundle>\n<No ID> / <No Location>");
        } else {
            sb.append(bundle.get().getSymbolicName());
            sb.append("\n");
            sb.append("ID#");
            sb.append(Long.toString(b.getBundleId()));
            sb.append(" / ");
            sb.append(b.getLocation());
        }
        sb.append("\n");
        Exception e = bundleException;
        if (e==null) {
            sb.append("--");
        } else {
            sb.append(e.getMessage());
        }
        sb.trimToSize();
        return sb.toString();
    }

}
