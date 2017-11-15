package ifml2.platform.player

import javafx.application.Application
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities
import javax.swing.JFrame
import javafx.embed.swing.JFXPanel
import javafx.application.Platform

private val MACOS_PROP_NAME = "javafx.macosx.embedded"
private val MACOS_PROP_VAL = "true"

val APP_TITLE = "IFML 2.1 - Player"

object PlayerFX {

    private val logger = LoggerFactory.getLogger(PlayerFX::class.java)

        @JvmStatic
    fun main(args: Array<String>) {
        startJFXPanel(args);
    }

    private fun allowMacOS() {
        System.setProperty(MACOS_PROP_NAME, MACOS_PROP_VAL)
    }

    private fun startJFXPanel(args: Array<String>) {
        allowMacOS()
        SwingUtilities.invokeLater {
            try {
                val mainFrame = JFrame(APP_TITLE)
                val jfxPanel = JFXPanel()
                mainFrame.apply {
                    contentPane.add(jfxPanel)
                    title = APP_TITLE
                    setSize(800, 600)
                    setLocationRelativeTo(null)
                    isVisible = true
                }
                Platform.runLater {
                    val fxui = PlayerFXUI()
                    try {
                        fxui.start(jfxPanel, mainFrame)
                    } catch (ex: Exception) {
                        logger.error("Unable start appliication...", ex)
                    }
                }
            } catch (ex: Throwable) {
                logger.error("Error while start application...", ex)
            }
        }
    }

    private fun startAWT(args: Array<String>) {
        allowMacOS()
        java.awt.Toolkit.getDefaultToolkit();
        Application.launch(PlayerFXUI::class.java, *args)
    }

}