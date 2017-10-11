package ifml2.platform.starter

import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javax.swing.JFrame
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
    System.setProperty("jaavafx.macosx.embedded", "true")
    SwingUtilities.invokeLater {
        val mainFrame = JFrame("gui tool (s/fx)")
        val jfxPanel = JFXPanel()
        mainFrame.contentPane.add(jfxPanel)
        mainFrame.title = "OSGI for (s/fx)"
        mainFrame.setSize(800, 600)
        mainFrame.setLocationRelativeTo(null)
        mainFrame.isVisible = true
        Platform.runLater {
            val fxui = MainFXUI()
            fxui.start(jfxPanel, mainFrame)
        }
    }
}