package ifml2.platform.core.fx

import javax.swing.JFrame
import javax.swing.SwingUtilities

import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.JFXPanel

object MainFX {

    @JvmStatic
    fun main(args: Array<String>) {
        // Two ways to start
        // (it is not trivial, as we want to show Swing components
        // too)
        //        startAWT(args);
        startJFXPanel(args)
    }

    private fun startJFXPanel(args: Array<String>) {
        System.setProperty("javafx.macosx.embedded", "true")
        SwingUtilities.invokeLater {
            try {
                val mainFrame = JFrame("gui tool (s/fx)")
                val jfxPanel = JFXPanel()
                mainFrame.contentPane.add(jfxPanel)
                mainFrame.title = "OSGi Snippets GUI Tool (s/fx)"
                mainFrame.setSize(800, 600)
                mainFrame.setLocationRelativeTo(null)
                mainFrame.isVisible = true
                Platform.runLater {
                    val fxui = MainFXUI()
                    try {
                        fxui.start(jfxPanel, mainFrame)
                    } catch (e: Exception) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun startAWT(args: Array<String>) {
        System.setProperty("javafx.macosx.embedded", "true")
        java.awt.Toolkit.getDefaultToolkit()
        Application.launch(MainFXUI::class.java, *args)
    }

}
