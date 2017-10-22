package ifml2.platform.core;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ifml2.platform.core.loader.OSGiFwLoader;

public class MainUI {

    Logger logger = LoggerFactory.getLogger(MainUI.class);

    JFrame mainFrame;
    JToolBar toolBar;

    JToolBar bundlesToolBar;
    JToolBar bundleFilesToolBar;

    JSplitPane splitPane;
    JSplitPane splitTablesPane;

    JPanel bundlesPanel;
    JPanel filesPanel;

    BundlesTableModel bundlesTableModel;
    JTable bundlesTable;
    JScrollPane bundlesScroll;
    boolean bundlesAreSelected;

    BundleFilesTableModel bundleFilesTableModel;
    JTable bundleFilesTable;
    JScrollPane bundleFilesScroll;
    boolean bundleFilesAreSelected;

    JTextArea messageText;
    JScrollPane messageScroll;

    // Main Actions:
    Action exitAction;
    Action aboutAction;

    // Framework Actions:
    Action startFrameworkAction;
    Action stopFrameworkAction;

    // Bundles Actions:
    Action startBundlesAction;
    Action stopBundlesAction;
    Action refreshBundlesAction;
    Action uninstallBundlesAction;

    // Files Actions:
    Action installFilesAction;
    Action uninstallFilesAction;
    Action refreshFilesAction;

    // The OSGiFwLoader loads and sets up the OSGi environment:
    OSGiFwLoader osgiFwLoader;

    // The DirectoryWatcher observes a directory for bundle files
    // and is used to update the file list in the user interface:
    DirectoryWatcher directoryWatcher;


    // (This is not part of the essential guitool features:)
    // Bundles shall be able to provide commands. This
    // tool bar shall show them:
    JToolBar pluginCmdToolBar;
//    CommandRegistryImpl commandRegistry;
//    CommandObserver commandObserver;

    public MainUI() {
        super();
    }

    public void open() {
        mainFrame = new JFrame("OSGi Snippets GUI Tool");

        Container con = mainFrame.getContentPane();

        // The main toolbar has the actions which are not specific
        // to bundles or bundle files:
        toolBar = new JToolBar();
        con.add(toolBar, BorderLayout.PAGE_START);
        toolBar.add(startFrameworkAction = new StartFrameworkAction());
        toolBar.add(stopFrameworkAction = new StopFrameworkAction());
        toolBar.add(exitAction = new ExitAction());
        toolBar.add(aboutAction = new AboutAction());

        // The plug-in tool bar:
        pluginCmdToolBar = new JToolBar();
        con.add(pluginCmdToolBar, BorderLayout.PAGE_END);
        pluginCmdToolBar.add(new JLabel("Commands from Bundles:"));
        // Create the CommandRegistry instance and attach it to the tool bar:
//        commandRegistry = new CommandRegistryImpl(pluginCmdToolBar);

        // Set up the three way split. First devides top and bottom
        // and the second divides the top into left and right.
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
        con.add(splitPane);
        splitTablesPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        splitPane.setLeftComponent(splitTablesPane);

        // Set up the table for the bundles:
        // (loaded bundles, not the bundle files)
        bundlesTableModel = new BundlesTableModel();
        bundlesTable = new JTable(bundlesTableModel);
        bundlesTable.setRowHeight(50);
        bundlesTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        bundlesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        bundlesTable.setDefaultRenderer(BundleEntry.class, new BundleCellRenderer());
        bundlesScroll = new JScrollPane(
                bundlesTable, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bundlesPanel = new JPanel(new BorderLayout());
        bundlesPanel.add(bundlesScroll);
        splitTablesPane.setLeftComponent(bundlesPanel);

        // The bundles toolbar goes into the panel for the bundles
        // table and contains the actions which are related to Bundle 
        // instances:
        bundlesToolBar = new JToolBar();
        bundlesPanel.add(bundlesToolBar, BorderLayout.PAGE_START);
        bundlesToolBar.add(startBundlesAction = new StartBundlesAction());
        bundlesToolBar.add(stopBundlesAction = new StopBundlesAction());
        bundlesToolBar.add(refreshBundlesAction = new RefreshBundlesAction());
        bundlesToolBar.add(uninstallBundlesAction = new UninstallBundlesAction());

        // Set up the table for the bundle files. These are the files
        // which are provided by the DirectoryWatcher.
        bundleFilesTableModel = new BundleFilesTableModel();
        bundleFilesTable = new JTable(bundleFilesTableModel);
        bundleFilesTable.setRowHeight(25);
        bundleFilesTable.setDefaultRenderer(BundleEntry.class, new BundleCellRenderer());
        bundleFilesTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        bundleFilesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        bundleFilesScroll = new JScrollPane(
                bundleFilesTable, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filesPanel = new JPanel(new BorderLayout());
        filesPanel.add(bundleFilesScroll);
        splitTablesPane.setRightComponent(filesPanel);

        // The files toolbar contains the file related actions
        // and goes into the panel for the files table:
        bundleFilesToolBar = new JToolBar();
        filesPanel.add(bundleFilesToolBar, BorderLayout.PAGE_START);
        bundleFilesToolBar.add(installFilesAction = new InstallFilesAction());
        bundleFilesToolBar.add(refreshFilesAction = new RefreshFilesAction());
        bundleFilesToolBar.add(uninstallFilesAction = new UninstallFilesAction());

        // Provide a message log which goes into the bottom UI area:
        messageText = new JTextArea();
        messageScroll = new JScrollPane(
                messageText, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        splitPane.setRightComponent(messageScroll);

        // Set up the shutdown behaviour (closing the window shall
        // call our code for closing and stopping the framework):
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doClose();
            }
        });

        // Initially there is no OSGi Framework, so 
        // start is enabled, stop is disabled:
        startFrameworkAction.setEnabled(true);
        stopFrameworkAction.setEnabled(false);

        updateBundleActionsState(false);
        updateBundleFileActionsState(false);
        bundlesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Update action state depending on whether any item is selected or not:
                boolean tmp = !bundlesTable.getSelectionModel().isSelectionEmpty();
                if (tmp==bundlesAreSelected) {
                    return;
                }
                bundlesAreSelected = tmp;
                updateBundleActionsState(tmp);
            }
        });
        bundleFilesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Update action state depending on whether any item is selected or not:
                boolean tmp = !bundleFilesTable.getSelectionModel().isSelectionEmpty();
                if (tmp==bundleFilesAreSelected) {
                    return;
                }
                bundleFilesAreSelected = tmp;
                updateBundleFileActionsState(tmp);
            }
        });

        // For now have a fixed size. Add remembering the 
        // window position later.
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        // Set some reasonable values for the dividers:
        // (bundle and files table get 50% in width, and 70% in height)
        splitTablesPane.setDividerLocation(0.5);
        splitPane.setDividerLocation(0.7);
    }

    protected void updateBundleActionsState(boolean hasSelection) {
        startBundlesAction.setEnabled(hasSelection);
        stopBundlesAction.setEnabled(hasSelection);
        uninstallBundlesAction.setEnabled(hasSelection);
        refreshBundlesAction.setEnabled(hasSelection);
    }

    protected void updateBundleFileActionsState(boolean hasSelection) {
        installFilesAction.setEnabled(hasSelection);
        uninstallFilesAction.setEnabled(hasSelection);
        refreshFilesAction.setEnabled(hasSelection);
    }

    protected void doClose() {
        doStopFramework(); // The method works well when already stopped.
        // Dispose the main frame and thus exit the application.
        // (Make sure, all still active threads are daemonized.)
        mainFrame.dispose();
    }

    protected void doAbout() {
        JOptionPane.showMessageDialog(
                mainFrame, 
                "osgisnippets guitool\n" +
                "https://sourceforge.net/p/osgisnippets/", 
                "About guitool", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Starts the OSGi Framework Loader, attaches the table models
     * to observe OSGi related events, adjusts the framework actions 
     * enable/disable status and creates the DirectoryWatcher.
     */
    protected void doStartFramework() {
        if (osgiFwLoader!=null) {
            
        } else {
            osgiFwLoader = new OSGiFwLoader();
            osgiFwLoader.start();
            bundlesTableModel.attach(osgiFwLoader.getBundleContext());
            bundleFilesTableModel.attach(osgiFwLoader.getBundleContext());
            startFrameworkAction.setEnabled(false);
            stopFrameworkAction.setEnabled(true);
            // Let the CommandRegistry be provided as a service:
//            commandRegistry.open(osgiFwLoader.getBundleContext());
            directoryWatcher = new DirectoryWatcher(new File("../common/bundles"), new WatcherAction());
            // Let the guitool look for Command services:
//            commandObserver = new CommandObserver(pluginCmdToolBar, osgiFwLoader.getBundleContext());
//            commandObserver.open();
        }
    }

    /**
     * Detaches the table models from the OSGi environment, stops
     * the DirectoryWatcher, stops the OSGi Framework and adjusts the
     * framework actions enable/disable state.
     */
    protected void doStopFramework() {
        if (osgiFwLoader!=null) {
            startFrameworkAction.setEnabled(false);
            stopFrameworkAction.setEnabled(false);

//            commandObserver.close();

            bundlesTableModel.detach();
            bundleFilesTableModel.detach();
            directoryWatcher.stop();
            osgiFwLoader.requestStop();

            // Prevent the call to waitForStop() from blocking the EDT:
            new SwingWorker<Object, Object>() {
                // This method runs outside the EDT. 
                // It can block without problems.
                // (Don't access Swing components in here.)
                @Override
                protected Object doInBackground() throws Exception {
                    osgiFwLoader.waitForStop(); // blocks
                    osgiFwLoader = null;
                    return null;
                }
                // This method runs in the EDT and it is run
                // after doInBackground() returns:
                // (Safely access Swing components in here.)
                @Override
                protected void done() {
                    // Let the CommandRegistry Service be unregistered:
//                    commandRegistry.close();

                    startFrameworkAction.setEnabled(true);
                    stopFrameworkAction.setEnabled(false);
                }
            }.execute();
            // (If this construct makes no sense to you: A SwingWorker 
            // subclass is created and its execute() method is called.)
        } else {
        }
    }

    /**
     * Returns a List of BundleEntrys which are currently selected in the 
     * bundle table. (This implementation supports a range of selection, not
     * multi selections.)
     * 
     * @return  Returns the List of currently selected BundleEntrys.
     */
    protected List<BundleEntry> getSelectedBundles() {
        ListSelectionModel selMdl = bundlesTable.getSelectionModel();
        if (selMdl.isSelectionEmpty())
            return null;
        int rowMin = selMdl.getMinSelectionIndex();
        int rowMax = selMdl.getMaxSelectionIndex();
        List<BundleEntry> rc = new ArrayList<>();
        for (int row=rowMin; row<=rowMax; row++) {
            if (!selMdl.isSelectedIndex(row)) {
                continue;
            }
            BundleEntry be = (BundleEntry) bundlesTableModel.getValueAt(row, 0);
            rc.add(be);
        }
        return rc;
    }

    /**
     * Returns a List of BundleEntrys which are currently selected in the 
     * files table. (This implementation supports a range of selection, not
     * multi selections.)
     * 
     * @return  Returns the List of currently selected BundleEntrys.
     */
    protected List<BundleEntry> getSelectedFiles() {
        ListSelectionModel selMdl = bundleFilesTable.getSelectionModel();
        if (selMdl.isSelectionEmpty())
            return null;
        int rowMin = selMdl.getMinSelectionIndex();
        int rowMax = selMdl.getMaxSelectionIndex();
        List<BundleEntry> rc = new ArrayList<>();
        for (int row=rowMin; row<=rowMax; row++) {
            if (!selMdl.isSelectedIndex(row)) {
                continue;
            }
            BundleEntry be = (BundleEntry) bundleFilesTableModel.getValueAt(row, 0);
            rc.add(be);
        }
        return rc;
    }

    /**
     * Calls Bundle.uninstall on the list of selected bundles.
     * If Exceptions are thrown, they are stored in the BundleEntry.
     */
    public void doUninstallBundles() {
        List<BundleEntry> bes = getSelectedBundles();
        if (bes==null) return;
        for (BundleEntry be : bes) {
            Bundle b = be.getBundle();
            try {
                b.uninstall();
            } catch (BundleException e) {
                logger.error("Error uninstalling bundle", e);
                be.setBundleException(e);
            }
        }
    }

    /**
     * Calls Bundle.start on the list of selected bundles.
     * If Exceptions are thrown, they are stored in the BundleEntry.
     */
    public void doStartBundles() {
        List<BundleEntry> bes = getSelectedBundles();
        if (bes==null) return;
        for (BundleEntry be : bes) {
            Bundle b = be.getBundle();
            try {
                b.start();
            } catch (BundleException|IllegalStateException e) {
                logger.error("Error starting bundle", e);
                be.setBundleException(e);
            }
        }
    }

    /**
     * Calls Bundle.stop on the list of selected bundles.
     * If Exceptions are thrown, they are stored in the BundleEntry.
     */
    public void doStopBundles() {
        List<BundleEntry> bes = getSelectedBundles();
        if (bes==null) return;
        for (BundleEntry be : bes) {
            Bundle b = be.getBundle();
            try {
                b.stop();
            } catch (BundleException|IllegalStateException e) {
                logger.error("Error stopping bundle", e);
                be.setBundleException(e);
            }
        }
    }

    /**
     * Performs a refresh on the selectd bundles.
     */
    public void doRefreshBundles() {
        List<BundleEntry> bes = getSelectedBundles();
        if (bes==null) return;
        List<Bundle> refreshList = new ArrayList<>();
        for (BundleEntry be : bes) {
            Bundle b = be.getBundle();
            refreshList.add(b);
        }
        if (refreshList.size()>0) {
            osgiFwLoader.refreshAndWait(refreshList);
        }
    }

    /**
     * Installs the selected files as bundles. The bundle for a file
     * is stored in the BundleEntry for that file.
     * Note that the bundle table does not need to be filled
     * here, as it gets filled using a BundleListener which relays
     * the events for adding bundles.
     */
    public void doInstallFiles() {
        List<BundleEntry> bes = getSelectedFiles();
        if (bes==null) return;
        List<File> files = new ArrayList<>();
        for (BundleEntry be : bes) {
            files.add(be.getFile());
        }
        List<Bundle> bundles = osgiFwLoader.installBundles(files);
        for (int i=0; i<bes.size(); i++) {
            bes.get(i).setBundle(bundles.get(i));
        }
    }

    /**
     * Finds the bundles for the selected file entries and calls
     * Bundle.uninstall on them. When exceptions are thrown, they 
     * are set in the BundleEntry.
     */
    public void doUninstallFiles() {
        List<BundleEntry> bes = getSelectedFiles();
        if (bes==null) return;
        for (BundleEntry be : bes) {
            Bundle b = be.getBundle();
            if (b==null) continue;
            try {
                b.uninstall();
            } catch (BundleException|IllegalStateException e) {
                logger.error("Error uninstalling bundle", e);
                be.setBundleException(e);
            }
        }
    }

    /**
     * Finds the Bundles for the selected files (if they are loaded)
     * and calls refresh on them.
     */
    public void doRefreshFiles() {
        List<BundleEntry> bes = getSelectedFiles();
        if (bes==null) return;
        List<Bundle> refreshList = new ArrayList<>();
        for (BundleEntry be : bes) {
            Bundle b = be.getBundle();
            if (b==null) continue;
            refreshList.add(b);
        }
        if (refreshList.size()>0) {
            osgiFwLoader.refreshAndWait(refreshList);
        }
    }

    /**
     * WatcherAction instances are registered with DirectoryWatchers to
     * receive notification when the file system content changes. The 
     * WatcherAction relays to the bundle files table model. 
     * (See {@link BundleFilesTableModel#updateWithWatchedFiles(LinkedHashMap)}.
     * 
     * @author rsc
     */
    class WatcherAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            // This cast causes an unchecked cast warning. 
            // The WatcherAction is only used with the DirectoryWatcher and that
            // always puts a LinkedHashMap into the ActionEvent's source property.
            LinkedHashMap<String, BundleEntry> changeSet = (LinkedHashMap<String, BundleEntry>) o;
            bundleFilesTableModel.updateWithWatchedFiles(changeSet);
        }
    }

    // ////////////////////////////////////////////////////////
    // Here come the Action classes. 
    // They provide a name for display in buttons and they
    // usually relay execution to a method of the MainUI class.
    // (for instance ExitAction relays to doClose() ) They 
    // are not declared static, so they have a reference to the 
    // enclosing MainUI instance.

    class ExitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExitAction() {
            super();
            putValue(NAME, "Exit");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doClose();
        }
        
    }

    class AboutAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public AboutAction() {
            super();
            putValue(NAME, "About");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doAbout();
        }
        
    }

    class StartFrameworkAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public StartFrameworkAction() {
            super();
            putValue(NAME, "Start Framework");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doStartFramework();
        }
        
    }

    class StopFrameworkAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public StopFrameworkAction() {
            super();
            putValue(NAME, "Stop Framework");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doStopFramework();
        }
        
    }

    class StartBundlesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public StartBundlesAction() {
            super();
            putValue(NAME, "Start");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doStartBundles();
        }
        
    }

    class StopBundlesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public StopBundlesAction() {
            super();
            putValue(NAME, "Stop");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doStopBundles();
        }
        
    }

    class RefreshBundlesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public RefreshBundlesAction() {
            super();
            putValue(NAME, "Refresh");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doRefreshBundles();
        }
    }

    class UninstallBundlesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public UninstallBundlesAction() {
            super();
            putValue(NAME, "Uninstall");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doUninstallBundles();
        }
    }

    class InstallFilesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public InstallFilesAction() {
            super();
            putValue(NAME, "Install");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInstallFiles();
        }
    }

    class UninstallFilesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public UninstallFilesAction() {
            super();
            putValue(NAME, "Uninstall");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doUninstallFiles();
        }
    }

    class RefreshFilesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        public RefreshFilesAction() {
            super();
            putValue(NAME, "Refresh");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doRefreshFiles();
        }
    }

}
