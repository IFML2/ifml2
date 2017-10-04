package ifml2.fx.player.view

import ifml2.engine.Engine
import ifml2.engine.featureproviders.text.OutputPlainTextProvider
import ifml2.engine.featureproviders.graphic.OutputIconProvider
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import ifml2.service.HistoryService
import ifml2.service.ServiceRegistry
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import tornadofx.View
import javax.swing.Icon

class PlayerView : View("FX ЯРИЛ 2.0 - Рассказчик"), OutputPlainTextProvider, OutputIconProvider {

    private val LOG = LoggerFactory.getLogger(PlayerView::class.java)

    override val root: BorderPane by fxml("/view/player.fxml")

    val restart: MenuItem by fxid()
    val loadGame: MenuItem by fxid()
    val saveGame: MenuItem by fxid()

    val scroller: ScrollPane by fxid()
    val outPanel: TextFlow by fxid()
    val comLine: TextField by fxid()

    val historyService = HistoryService()
    val engine: Engine = ServiceRegistry.getEngine(this, this);

    fun onNewStory() {
        outputPlainText("NEW STORY")
    }

    fun onRestartStory() {
        outputPlainText("RESTART STORY")
    }

    fun onLoadGame() {
        val fileChooser = FileChooser()
        fileChooser.title = "Загрузить игру"
        val story = fileChooser.showOpenDialog(this.currentWindow)
        if (story != null) {
            if (story.exists()) {
                engine.loadGame(story.absolutePath)
            } else {
                outputPlainText("Выбранный файл не существует.")
            }
        }
    }

    fun onSaveGame() {
        val fileChooser = FileChooser()
        fileChooser.title = "Сохранить игру"
        val story = fileChooser.showSaveDialog(this.currentWindow)
        if (story != null) {
            engine.saveGame(story.absolutePath)
        } else {
            outputPlainText("Сохранение отменено")
        }
    }

    fun onExit() {
        System.exit(0)
    }

    fun onKeyPressed(event: KeyEvent) {
        when (event.code) {
            KeyCode.ENTER -> processCommand(getCommand())
            KeyCode.UP -> comLine.text = historyService.prev()
            KeyCode.DOWN -> comLine.text = historyService.next()
            else -> {
                // Nothing to do
            }
        }
    }

    private fun getCommand(): String {
        val command = comLine.text;
        comLine.text = ""
        return command
    }

    private fun echoCommand(command: String) {
        outputPlainText("\n")
        outputPlainText("> $command\n")
        scroller.layout()
        scroller.vvalue = 1.0
    }

    private fun processCommand(command: String) {
        echoCommand(command);

        if (command.isEmpty()) {
            outputPlainText("Введите что-нибудь.\n")
            return
        }

        historyService.update(command)

        if (command.startsWith("/")) {
            //TODO: Подумать. Тут должен быть список из известных плееру комманд
            when (command.toLowerCase()) {
                "/заново" -> onRestartStory()
                "/сохранить" -> onSaveGame()
                "/загзузить" -> onLoadGame()
            }
        } else {
            engine.executeGamerCommand(command)
        }
    }

    override fun outputPlainText(text: String) {
        outPanel.add(Text(text))
    }

    override fun outputIcon(icon: Icon?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
