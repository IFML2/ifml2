package ifml2.platform.core.fx

import java.awt.event.WindowAdapter
import java.io.File
import java.util.ArrayList
import java.util.LinkedHashMap

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.concurrent.Service
import javafx.concurrent.Task
import javafx.embed.swing.JFXPanel
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.SelectionMode
import javafx.scene.control.SplitPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.ToolBar
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import javafx.util.Callback

import javax.swing.JFrame
import javax.swing.SwingUtilities

import org.osgi.framework.Bundle
import org.osgi.framework.BundleException
import org.slf4j.LoggerFactory

import ifml2.platform.core.fx.BundleEntry.FileState
import ifml2.platform.core.loader.OSGiFwLoader

class MainFXUI : Application() {

    internal var logger = LoggerFactory.getLogger(MainFXUI::class.java)

    // Used when started as pure JavaFX app:
    @Volatile internal var mainStage: Stage? = null
    // Used when started being aware of Swing:
    @Volatile internal var mainFrame: JFrame? = null

    internal var mainToolBar: ToolBar? = null
    internal var exitButton: Button? = null
    internal var startFwButton: Button? = null
    internal var stopFwButton: Button? = null

    internal var mainSplitPane: SplitPane? = null
    internal var textArea: TextArea? = null

    internal var tablesSplitPane: SplitPane? = null

    internal var bundlesPane: BorderPane? = null
    internal var bundlesToolBar: ToolBar? = null
    internal var bundlesTableView: TableView<BundleEntry>? = null
    internal var bundlesTable = BundlesTable()

    internal var startBundlesButton: Button? = null
    internal var stopBundlesButton: Button? = null
    internal var refreshBundlesButton: Button? = null
    internal var uninstallBundlesButton: Button? = null

    internal var filesPane: BorderPane? = null
    internal var filesToolBar: ToolBar? = null
    internal var filesTableView: TableView<BundleEntry>? = null
    internal var filesTable = FilesTable()

    internal var installFilesButton: Button? = null
    internal var refreshFilesButton: Button? = null
    internal var uninstallFilesButton: Button? = null

    internal var osgiFwLoader: OSGiFwLoader? = null
    internal var directoryWatcher: DirectoryWatcher? = null

    /**
     * Returns a List of BundleEntry instances which are currently selected
     * in the bundle table.
     *
     * @return  Returns the List of currently selected BundleEntry items.
     */
    protected// Return a copy of the list:
    val selectedBundles: List<BundleEntry>
        get() {
            val selection = bundlesTableView?.selectionModel?.selectedItems
            return ArrayList(selection)
        }

    /**
     * Returns a List of BundleEntry instances which are currently selected in the
     * files table.
     *
     * @return  Returns the List of currently selected BundleEntry items.
     */
    protected// Return a copy of the list:
    val selectedFiles: List<BundleEntry>
        get() {
            val selection = filesTableView?.selectionModel?.selectedItems
            return ArrayList(selection)
        }

    /**
     * This method is called by the JavaFX framework when the application
     * is started with Application.launch(). (See MainFX class)
     */
    @Throws(Exception::class)
    override fun start(stage: Stage) {
        mainStage = stage
        mainStage!!.title = "OSGi Snippets GUI Tool (fx)"
        val mainScene = createMainScene()
        mainStage!!.scene = mainScene
        mainStage!!.onCloseRequest = EventHandler {
            // When mainStage is not null, the main window has been closed.
            // When mainStage is null, the Exit button was pressed.
            // Only in the first case, doExit() shall be called.
            if (mainStage != null) {
                doExit()
            }
        }
        mainStage!!.show()
    }

    /**
     * This method is called from MainFX when the gui tool shall be embedded
     * in a Swing JFrame.
     *
     * @param jfxPanel  A JFXPanel in which the JavaFX components shall be added.
     * @param frame A JFrame which is the main window of the gui tool. A
     * WindowListener is registered with the JFrame to quit
     * when the main window is closed.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun start(jfxPanel: JFXPanel, frame: JFrame) {
        this.mainFrame = frame
        val mainScene = createMainScene()
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                // When mainFrame is not null, the main window has been
                // closed. If mainFrame is null, the exit button was pressed.
                // Only in the first case call doExit():
                if (mainFrame != null) {
                    Platform.runLater { doExit() }
                } else {
                    super.windowClosing(e)
                }
            }
        })
        jfxPanel.scene = mainScene
    }

    fun createMainScene(): Scene {
        val mainPane = BorderPane()
        val mainScene = Scene(mainPane, 800.0, 600.0)

        mainToolBar = ToolBar()
        mainPane.top = mainToolBar

        mainSplitPane = SplitPane()
        mainSplitPane?.orientation = Orientation.VERTICAL
        mainPane.center = mainSplitPane

        tablesSplitPane = SplitPane()
        tablesSplitPane?.orientation = Orientation.HORIZONTAL

        textArea = TextArea()

        mainSplitPane?.items?.add(tablesSplitPane)
        mainSplitPane?.items?.add(textArea)
        mainSplitPane!!.dividers[0].position = 0.7

        bundlesPane = BorderPane()
        filesPane = BorderPane()

        tablesSplitPane?.items?.add(bundlesPane)
        tablesSplitPane?.items?.add(filesPane)
        mainSplitPane!!.dividers[0].position = 0.5

        bundlesToolBar = ToolBar()
        bundlesPane?.top = bundlesToolBar

        filesToolBar = ToolBar()
        filesPane?.top = filesToolBar

        bundlesTableView = TableView(bundlesTable.entries)
        bundlesTableView?.selectionModel?.selectionMode = SelectionMode.MULTIPLE
        bundlesPane?.center = bundlesTableView
        val bundlesColumn1 = TableColumn<BundleEntry, String>("Details")
        bundlesColumn1.cellValueFactory = PropertyValueFactory("bundleDetails")
        val bundlesColumn2 = TableColumn<BundleEntry, String>("Status")
        bundlesColumn2.cellValueFactory = PropertyValueFactory("bundleState")

        bundlesTableView?.columns?.add(bundlesColumn1)
        bundlesTableView?.columns?.add(bundlesColumn2)
        bundlesColumn1?.prefWidthProperty().bind(bundlesTableView?.widthProperty()?.divide(4)?.multiply(2.9)) // w * 3/4
        bundlesColumn2?.prefWidthProperty().bind(bundlesTableView?.widthProperty()?.divide(4)?.multiply(0.9)) // w * 1/4

        filesTableView = TableView(filesTable.entries)
        filesTableView?.selectionModel?.selectionMode = SelectionMode.MULTIPLE
        filesPane?.center = filesTableView
        val filesColumn1 = TableColumn<BundleEntry, String>("Details")
        filesColumn1.cellValueFactory = Callback { p -> ReadOnlyObjectWrapper(p.value.getFile().name) }
        val filesColumn2 = TableColumn<BundleEntry, String>("Status")
        filesColumn2.cellValueFactory = PropertyValueFactory("fileState")
        filesTableView?.columns?.add(filesColumn1)
        filesTableView?.columns?.add(filesColumn2)
        filesColumn1.prefWidthProperty().bind(filesTableView?.widthProperty()?.divide(4)?.multiply(2.9)) // w * 3/4
        filesColumn2.prefWidthProperty().bind(filesTableView?.widthProperty()?.divide(4)?.multiply(0.9)) // w * 1/4

        // The buttons:
        exitButton = Button("Exit")
        startFwButton = Button("Start Framework")
        stopFwButton = Button("Stop Framework")
        mainToolBar?.items?.add(exitButton)
        mainToolBar?.items?.add(startFwButton)
        mainToolBar?.items?.add(stopFwButton)

        startBundlesButton = Button("Start")
        stopBundlesButton = Button("Stop")
        uninstallBundlesButton = Button("Uninstall")
        refreshBundlesButton = Button("Refresh")
        bundlesToolBar?.items?.add(startBundlesButton)
        bundlesToolBar?.items?.add(stopBundlesButton)
        bundlesToolBar?.items?.add(uninstallBundlesButton)
        bundlesToolBar?.items?.add(refreshBundlesButton)

        installFilesButton = Button("Install")
        uninstallFilesButton = Button("Uninstall")
        refreshFilesButton = Button("Refresh")
        filesToolBar?.items?.add(installFilesButton)
        filesToolBar?.items?.add(uninstallFilesButton)
        filesToolBar?.items?.add(refreshFilesButton)

        wireButtons()
        return mainScene
    }

    protected fun doExit() {
        doStopFw()
        // Close in case of JavaFX UI:
        if (mainStage != null) {
            val st = mainStage
            mainStage = null
            st!!.close()
        }
        // Close in case of Swing wrapped JavaFX UI:
        if (mainFrame != null) {
            val f = mainFrame
            mainFrame = null
            // Swing operations must be performed in EDT:
            SwingUtilities.invokeLater { f!!.dispose() }
        }
    }

    protected fun doStartFw() {
        if (osgiFwLoader != null) {

        } else {
            osgiFwLoader = OSGiFwLoader()
            osgiFwLoader!!.start()
            bundlesTable.attach(osgiFwLoader!!.bundleContext)
            filesTable.attach(osgiFwLoader!!.bundleContext)
            startFwButton?.isDisable = true
            stopFwButton?.isDisable = false
            // Let the CommandRegistry be provided as a service:
            // (Disabled now, remains from Swing variant)
            //            commandRegistry.open(osgiFwLoader.getBundleContext());
            directoryWatcher = DirectoryWatcher(File("../common/bundles"), WatcherAction())
            // Let the guitool look for Command services:
            // (Disabled now, remains from Swing variant)
            //            commandObserver = new CommandObserver(pluginCmdToolBar, osgiFwLoader.getBundleContext());
            //            commandObserver.open();
        }

    }

    protected fun doStopFw() {
        if (osgiFwLoader != null) {
            startFwButton?.isDisable = true
            stopFwButton?.isDisable = true

            // (Disabled now, remains from Swing variant)
            //            commandObserver.close();

            bundlesTable.detach()
            filesTable.detach()
            directoryWatcher!!.stop()
            osgiFwLoader!!.requestStop()

            val svc = object : Service<Any>() {
                override fun createTask(): Task<Any> {
                    return object : Task<Any>() {
                        @Throws(Exception::class)
                        override fun call(): Any? {
                            osgiFwLoader!!.waitForStop() // blocks
                            osgiFwLoader = null
                            Platform.runLater {
                                // Let the CommandRegistry Service be unregistered:
                                //                                    commandRegistry.close();

                                startFwButton?.isDisable = false
                                stopFwButton?.isDisable = true
                            }
                            return null
                        }
                    }
                }
            }
            svc.start()
        } else {
            // osgiFwLoader==null, nothing to stop
        }
    }

    /**
     * Calls Bundle.uninstall on the list of selected bundles.
     * If Exceptions are thrown, they are stored in the BundleEntry.
     */
    fun doUninstallBundles() {
        val bes = selectedBundles ?: return
        for (be in bes) {
            val b = be.getBundle()
            try {
                b.uninstall()
            } catch (e: BundleException) {
                logger.error("Error uninstalling bundle", e)
                be.setBundleException(e)
            }

        }
    }

    /**
     * Calls Bundle.start on the list of selected bundles.
     * If Exceptions are thrown, they are stored in the BundleEntry.
     */
    fun doStartBundles() {
        val bes = selectedBundles ?: return
        for (be in bes) {
            val b = be.getBundle()
            try {
                b.start()
            } catch (e: BundleException) {
                logger.error("Error starting bundle", e)
                be.setBundleException(e)
            } catch (e: IllegalStateException) {
                logger.error("Error starting bundle", e)
                be.setBundleException(e)
            }

        }
    }

    /**
     * Calls Bundle.stop on the list of selected bundles.
     * If Exceptions are thrown, they are stored in the BundleEntry.
     */
    fun doStopBundles() {
        val bes = selectedBundles ?: return
        for (be in bes) {
            val b = be.getBundle()
            try {
                b.stop()
            } catch (e: BundleException) {
                logger.error("Error stopping bundle", e)
                be.setBundleException(e)
            } catch (e: IllegalStateException) {
                logger.error("Error stopping bundle", e)
                be.setBundleException(e)
            }

        }
    }

    /**
     * Performs a refresh on the selected bundles.
     */
    fun doRefreshBundles() {
        val bes = selectedBundles ?: return
        val refreshList = ArrayList<Bundle>()
        for (be in bes) {
            val b = be.getBundle()
            refreshList.add(b)
        }
        if (refreshList.size > 0) {
            osgiFwLoader!!.refreshAndWait(refreshList)
        }
    }

    /**
     * Installs the selected files as bundles. The bundle for a file
     * is stored in the BundleEntry for that file.
     * Note that the bundle table does not need to be filled
     * here, as it gets filled using a BundleTracker which relays
     * the events for adding bundles.
     */
    fun doInstallFiles() {
        val bes = selectedFiles ?: return
        val files = ArrayList<File>()
        for (be in bes) {
            files.add(be.getFile())
        }
        // FIXME: between install and marking unchanged, lock against concurrent modification? (don't care for now)
        val bundles = osgiFwLoader!!.installBundles(files)
        for (i in bes.indices) {
            bes[i].setBundle(bundles[i])
            bes[i].setFileState(FileState.CURRENT)
        }
    }

    /**
     * Finds the bundles for the selected file entries and calls
     * Bundle.uninstall on them. When exceptions are thrown, they
     * are set in the BundleEntry.
     */
    fun doUninstallFiles() {
        val bes = selectedFiles ?: return
        for (be in bes) {
            val b = be.getBundle() ?: continue
            try {
                b.uninstall()
            } catch (e: BundleException) {
                logger.error("Error uninstalling bundle", e)
                be.setBundleException(e)
            } catch (e: IllegalStateException) {
                logger.error("Error uninstalling bundle", e)
                be.setBundleException(e)
            }

        }
    }

    /**
     * Finds the Bundles for the selected files (if they are loaded)
     * and calls refresh on them.
     */
    fun doRefreshFiles() {
        val bes = selectedFiles ?: return
        val refreshList = ArrayList<Bundle>()
        for (be in bes) {
            val b = be.getBundle() ?: continue
            refreshList.add(b)
        }
        if (refreshList.size > 0) {
            osgiFwLoader!!.refreshAndWait(refreshList)
        }
    }

    private fun wireButtons() {
        exitButton?.onAction = EventHandler { doExit() }
        startFwButton?.onAction = EventHandler { doStartFw() }
        stopFwButton?.onAction = EventHandler { doStopFw() }
        startBundlesButton?.onAction = EventHandler { doStartBundles() }
        stopBundlesButton?.onAction = EventHandler { doStopBundles() }
        uninstallBundlesButton?.onAction = EventHandler { doUninstallBundles() }
        refreshBundlesButton?.onAction = EventHandler { doRefreshBundles() }
        installFilesButton?.onAction = EventHandler { doInstallFiles() }
        uninstallFilesButton?.onAction = EventHandler { doUninstallFiles() }
        refreshFilesButton?.onAction = EventHandler { doRefreshFiles() }
    }


    /**
     * WatcherAction instances are registered with DirectoryWatchers to
     * receive notification when the file system content changes. The
     * WatcherAction relays to the bundle files table model.
     * (See [BundleFilesTableModel.updateWithWatchedFiles].
     *
     * @author rsc
     */
    internal inner class WatcherAction : EventHandler<ActionEvent> {
        override fun handle(event: ActionEvent) {
            val o = event.source
            // This cast causes an unchecked cast warning.
            // The WatcherAction is only used with the DirectoryWatcher and that
            // always puts a LinkedHashMap into the ActionEvent's source property.
            val changeSet = o as LinkedHashMap<String, BundleEntry>
            filesTable.updateWithWatchedFiles(changeSet)
        }
    }

}
