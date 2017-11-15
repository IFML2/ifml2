package ifml2.platform.player

import javafx.application.Application
import javafx.stage.Stage
import javafx.embed.swing.JFXPanel
import javax.swing.JFrame
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import java.awt.event.WindowAdapter
import javafx.application.Platform
import javafx.event.EventHandler
import ifml2.platform.player.osgi.FelixBootstrap
import javax.swing.SwingUtilities

class PlayerFXUI : Application() {

    @Volatile internal var mainStage: Stage? = null
    @Volatile internal var mainFrame: JFrame? = null

    internal var felixBootstrap: FelixBootstrap? = null

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        mainStage = stage
        mainStage?.apply {
            title = "IFML 2.1 - Player"
            scene = createMainScene()
            onCloseRequest = EventHandler {
                if (mainStage != null) {
                    doExit()
                }
            }
        }
        mainStage?.show()
    }

    @Throws(Exception::class)
    fun start(jfxPanel: JFXPanel, jFrame: JFrame) {
        this.mainFrame = jFrame
        val mainScene = createMainScene()
        jFrame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(event: java.awt.event.WindowEvent?) {
                if (mainFrame != null) {
                    Platform.runLater { doExit() }
                } else {
                    super.windowClosed(event)
                }
            }
        })
        jfxPanel.scene = mainScene
    }

    private fun createMainScene(): Scene {
        val mainPane = BorderPane()
        val mainScene = Scene(mainPane, 800.0, 600.0)
        // other init stuff
        return mainScene
    }

    private fun doExit() {
        doStopFw()
        if (mainStage != null) {
            val tempStage = mainStage
            mainStage = null
            tempStage?.close()
        }
        if (mainFrame != null) {
            val tempFrame = mainFrame
            mainFrame = null
            SwingUtilities.invokeLater { tempFrame?.dispose() }
        }
    }

    private fun doStartFw() {
    }

    private fun doStopFw() {}

}