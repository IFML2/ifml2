package ifml2.platform.starter

import ifml2.platform.common.OsgiFwLoader
import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.awt.event.WindowAdapter
import javax.swing.JFrame
import javax.swing.SwingUtilities


class MainFXUI() : Application() {
    var mainStage: Stage? = null
    var mainFrame: JFrame? = null
    var osgiFwLoader: OsgiFwLoader? = null

    override fun start(stage: Stage?) {
        mainStage = stage
        mainStage?.title = "OSGi Starter"
        mainStage?.setScene(createMainScene())
        mainStage?.setOnCloseRequest { if (mainStage != null) { doExit() } }
        mainStage?.show()
    }

    fun start(jfxPanel: JFXPanel, frame: JFrame) {
        this.mainFrame = frame
        val mainScene = createMainScene()
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                if (mainFrame != null) {
                    Platform.runLater { doExit() }
                } else {
                    super.windowClosing(e)
                }
            }
        })
        jfxPanel.scene = mainScene
    }

    private fun createMainScene(): Scene {
        return Scene(BorderPane())
    }

    fun doExit() {
        doStopFw()
        if (mainStage != null) {
            val stage = mainStage!!
            mainStage = null;
            stage.close()
        }
        if (mainFrame != null) {
            val frame = mainFrame!!
            mainFrame = null
            SwingUtilities.invokeLater { frame.dispose() }
        }
    }

    fun doStartFw() {
        if (osgiFwLoader == null) {
            val loader = OsgiFwLoader()
            loader.start()
            osgiFwLoader = loader
        }
    }

    fun doStopFw() {
        if (osgiFwLoader != null) {
            val loader = osgiFwLoader!!
            osgiFwLoader = null
            loader.requestStop()
        }
    }

}