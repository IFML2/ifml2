package ifml2.platform.core.fx;

import java.awt.event.WindowAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ifml2.platform.core.fx.BundleEntry.FileState;
import ifml2.platform.core.loader.OSGiFwLoader;

public class MainFXUI extends Application {

    Logger logger = LoggerFactory.getLogger(MainFXUI.class);

    // Used when started as pure JavaFX app:
    volatile Stage mainStage;
    // Used when started being aware of Swing:
    volatile JFrame mainFrame;

    ToolBar mainToolBar;
    Button exitButton;
    Button startFwButton;
    Button stopFwButton;

    SplitPane mainSplitPane;
    TextArea textArea;

    SplitPane tablesSplitPane;

    BorderPane bundlesPane;
    ToolBar bundlesToolBar;
    TableView<BundleEntry> bundlesTableView;
    BundlesTable bundlesTable = new BundlesTable();

    Button startBundlesButton;
    Button stopBundlesButton;
    Button refreshBundlesButton;
    Button uninstallBundlesButton;

    BorderPane filesPane;
    ToolBar filesToolBar;
    TableView<BundleEntry> filesTableView;
    FilesTable filesTable = new FilesTable();

    Button installFilesButton;
    Button refreshFilesButton;
    Button uninstallFilesButton;

    OSGiFwLoader osgiFwLoader = null;
    DirectoryWatcher directoryWatcher = null;

    /**
     * This method is called by the JavaFX framework when the application
     * is started with Application.launch(). (See MainFX class)
     */
    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        mainStage.setTitle("OSGi Snippets GUI Tool (fx)");
        Scene mainScene = createMainScene();
        mainStage.setScene(mainScene);
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                // When mainStage is not null, the main window has been closed.
                // When mainStage is null, the Exit button was pressed.
                // Only in the first case, doExit() shall be called.
                if (mainStage!=null) {
                    doExit();
                }
            }
        });
        mainStage.show();
    }

    /**
     * This method is called from MainFX when the gui tool shall be embedded 
     * in a Swing JFrame.
     * 
     * @param jfxPanel  A JFXPanel in which the JavaFX components shall be added.
     * @param frame A JFrame which is the main window of the gui tool. A 
     *              WindowListener is registered with the JFrame to quit
     *              when the main window is closed.
     * @throws Exception
     */
    public void start(JFXPanel jfxPanel, JFrame frame) throws Exception {
        this.mainFrame = frame;
        Scene mainScene = createMainScene();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // When mainFrame is not null, the main window has been
                // closed. If mainFrame is null, the exit button was pressed.
                // Only in the first case call doExit():
                if (mainFrame!=null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            doExit();
                        }
                    });
                } else {
                    super.windowClosing(e);
                }
            }
        });
        jfxPanel.setScene(mainScene);
    }

    public Scene createMainScene() {
        BorderPane mainPane = new BorderPane();
        Scene mainScene = new Scene(mainPane, 800, 600);

        mainToolBar = new ToolBar();
        mainPane.setTop(mainToolBar);

        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainPane.setCenter(mainSplitPane);

        tablesSplitPane = new SplitPane();
        tablesSplitPane.setOrientation(Orientation.HORIZONTAL);

        textArea = new TextArea();

        mainSplitPane.getItems().add(tablesSplitPane);
        mainSplitPane.getItems().add(textArea);
        mainSplitPane.getDividers().get(0).setPosition(0.7);

        bundlesPane = new BorderPane();
        filesPane = new BorderPane();

        tablesSplitPane.getItems().add(bundlesPane);
        tablesSplitPane.getItems().add(filesPane);
        mainSplitPane.getDividers().get(0).setPosition(0.5);

        bundlesToolBar = new ToolBar();
        bundlesPane.setTop(bundlesToolBar);

        filesToolBar = new ToolBar();
        filesPane.setTop(filesToolBar);

        bundlesTableView = new TableView<>(bundlesTable.getEntries());
        bundlesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bundlesPane.setCenter(bundlesTableView);
        TableColumn<BundleEntry, String> bundlesColumn1 = new TableColumn<>("Details");
        bundlesColumn1.setCellValueFactory(new PropertyValueFactory<BundleEntry, String>("bundleDetails"));
        TableColumn<BundleEntry, String> bundlesColumn2 = new TableColumn<>("Status");
        bundlesColumn2.setCellValueFactory(new PropertyValueFactory<BundleEntry, String>("bundleState"));

        bundlesTableView.getColumns().add(bundlesColumn1);
        bundlesTableView.getColumns().add(bundlesColumn2);
        bundlesColumn1.prefWidthProperty().bind(bundlesTableView.widthProperty().divide(4).multiply(2.9)); // w * 3/4
        bundlesColumn2.prefWidthProperty().bind(bundlesTableView.widthProperty().divide(4).multiply(0.9)); // w * 1/4

        filesTableView = new TableView<>(filesTable.getEntries());
        filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesPane.setCenter(filesTableView);
        TableColumn<BundleEntry, String> filesColumn1 = new TableColumn<>("Details");
        filesColumn1.setCellValueFactory(new Callback<CellDataFeatures<BundleEntry, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<BundleEntry, String> p) {
                return new ReadOnlyObjectWrapper<String>(p.getValue().getFile().getName());
            }
        });
        TableColumn<BundleEntry, String> filesColumn2 = new TableColumn<>("Status");
        filesColumn2.setCellValueFactory(new PropertyValueFactory<BundleEntry, String>("fileState"));
        filesTableView.getColumns().add(filesColumn1);
        filesTableView.getColumns().add(filesColumn2);
        filesColumn1.prefWidthProperty().bind(filesTableView.widthProperty().divide(4).multiply(2.9)); // w * 3/4
        filesColumn2.prefWidthProperty().bind(filesTableView.widthProperty().divide(4).multiply(0.9)); // w * 1/4

        // The buttons:
        exitButton = new Button("Exit");
        startFwButton = new Button("Start Framework");
        stopFwButton = new Button("Stop Framework");
        mainToolBar.getItems().add(exitButton);
        mainToolBar.getItems().add(startFwButton);
        mainToolBar.getItems().add(stopFwButton);

        startBundlesButton = new Button("Start");
        stopBundlesButton = new Button("Stop");
        uninstallBundlesButton = new Button("Uninstall");
        refreshBundlesButton = new Button("Refresh");
        bundlesToolBar.getItems().add(startBundlesButton);
        bundlesToolBar.getItems().add(stopBundlesButton);
        bundlesToolBar.getItems().add(uninstallBundlesButton);
        bundlesToolBar.getItems().add(refreshBundlesButton);

        installFilesButton = new Button("Install");
        uninstallFilesButton = new Button("Uninstall");
        refreshFilesButton = new Button("Refresh");
        filesToolBar.getItems().add(installFilesButton);
        filesToolBar.getItems().add(uninstallFilesButton);
        filesToolBar.getItems().add(refreshFilesButton);

        wireButtons();
        return mainScene;
    }

    protected void doExit() {
        doStopFw();
        // Close in case of JavaFX UI:
        if (mainStage!=null) {
            Stage st = mainStage;
            mainStage = null;
            st.close();
        }
        // Close in case of Swing wrapped JavaFX UI:
        if (mainFrame!=null) {
            final JFrame f = mainFrame;
            mainFrame = null;
            // Swing operations must be performed in EDT:
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    f.dispose();
                }
            });
        }
    }

    protected void doStartFw() {
        if (osgiFwLoader!=null) {
            
        } else {
            osgiFwLoader = new OSGiFwLoader();
            osgiFwLoader.start();
            bundlesTable.attach(osgiFwLoader.getBundleContext());
            filesTable.attach(osgiFwLoader.getBundleContext());
            startFwButton.setDisable(true);
            stopFwButton.setDisable(false);
            // Let the CommandRegistry be provided as a service:
            // (Disabled now, remains from Swing variant)
//            commandRegistry.open(osgiFwLoader.getBundleContext());
            directoryWatcher = new DirectoryWatcher(new File("../common/bundles"), new WatcherAction());
            // Let the guitool look for Command services:
            // (Disabled now, remains from Swing variant)
//            commandObserver = new CommandObserver(pluginCmdToolBar, osgiFwLoader.getBundleContext());
//            commandObserver.open();
        }

    }

    protected void doStopFw() {
        if (osgiFwLoader!=null) {
            startFwButton.setDisable(true);
            stopFwButton.setDisable(true);

            // (Disabled now, remains from Swing variant)
//            commandObserver.close();

            bundlesTable.detach();
            filesTable.detach();
            directoryWatcher.stop();
            osgiFwLoader.requestStop();

            Service<Object> svc = new Service<Object>() {
                @Override
                protected Task<Object> createTask() {
                    return new Task<Object>() {
                        @Override
                        protected Object call() throws Exception {
                            osgiFwLoader.waitForStop(); // blocks
                            osgiFwLoader = null;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Let the CommandRegistry Service be unregistered:
//                                    commandRegistry.close();

                                    startFwButton.setDisable(false);
                                    stopFwButton.setDisable(true);
                                }
                            });
                            return null;
                        }
                    };
                }
            };
            svc.start();
        } else {
            // osgiFwLoader==null, nothing to stop
        }
    }

    /**
     * Returns a List of BundleEntry instances which are currently selected 
     * in the bundle table.
     * 
     * @return  Returns the List of currently selected BundleEntry items.
     */
    protected List<BundleEntry> getSelectedBundles() {
        ObservableList<BundleEntry> selection = bundlesTableView.getSelectionModel().getSelectedItems();
        // Return a copy of the list:
        List<BundleEntry> rc = new ArrayList<>(selection);
        return rc;
    }

    /**
     * Returns a List of BundleEntry instances which are currently selected in the 
     * files table.
     * 
     * @return  Returns the List of currently selected BundleEntry items.
     */
    protected List<BundleEntry> getSelectedFiles() {
        ObservableList<BundleEntry> selection = filesTableView.getSelectionModel().getSelectedItems();
        // Return a copy of the list:
        List<BundleEntry> rc = new ArrayList<>(selection);
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
     * Performs a refresh on the selected bundles.
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
     * here, as it gets filled using a BundleTracker which relays
     * the events for adding bundles.
     */
    public void doInstallFiles() {
        List<BundleEntry> bes = getSelectedFiles();
        if (bes==null) return;
        List<File> files = new ArrayList<>();
        for (BundleEntry be : bes) {
            files.add(be.getFile());
        }
        // FIXME: between install and marking unchanged, lock against concurrent modification? (don't care for now)
        List<Bundle> bundles = osgiFwLoader.installBundles(files);
        for (int i=0; i<bes.size(); i++) {
            bes.get(i).setBundle(bundles.get(i));
            bes.get(i).setFileState(FileState.CURRENT);
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

    private void wireButtons() {
        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doExit();
            }
        });
        startFwButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doStartFw();
            }
        });
        stopFwButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doStopFw();
            }
        });
        startBundlesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doStartBundles();
            }
        });
        stopBundlesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doStopBundles();
            }
        });
        uninstallBundlesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doUninstallBundles();
            }
        });
        refreshBundlesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doRefreshBundles();
            }
        });
        installFilesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doInstallFiles();
            }
        });
        uninstallFilesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doUninstallFiles();
            }
        });
        refreshFilesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doRefreshFiles();
            }
        });
    }


    /**
     * WatcherAction instances are registered with DirectoryWatchers to
     * receive notification when the file system content changes. The 
     * WatcherAction relays to the bundle files table model. 
     * (See {@link BundleFilesTableModel#updateWithWatchedFiles(LinkedHashMap)}.
     * 
     * @author rsc
     */
    class WatcherAction implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            Object o = event.getSource();
            // This cast causes an unchecked cast warning. 
            // The WatcherAction is only used with the DirectoryWatcher and that
            // always puts a LinkedHashMap into the ActionEvent's source property.
            LinkedHashMap<String, BundleEntry> changeSet = (LinkedHashMap<String, BundleEntry>) o;
            filesTable.updateWithWatchedFiles(changeSet);
        }
    }

}
