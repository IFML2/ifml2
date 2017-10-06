package ifml2.fx.player.view

import ifml2.engine.Engine
import ifml2.engine.featureproviders.graphic.OutputIconProvider
import ifml2.engine.featureproviders.text.OutputPlainTextProvider
import ifml2.service.HistoryService
import ifml2.service.ServiceRegistry
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
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
    val engine: Engine = ServiceRegistry.getEngine(this, this)

    var story: File? = null
    var saved: Boolean = true

    private fun loadStory() {
        if (story != null && story!!.exists()) {
            outputPlainText("Загрузка...\n\n")
            engine.loadStory(story?.absolutePath, true)
            comLine.text = ""
            saved = true
            engine.initGame()
            scroller.layout()
            scroller.vvalue = 1.0
        } else {
            outputPlainText("Файл истории не существует.")
        }
    }

    fun onNewStory() {
        val fileChooser = FileChooser()
        fileChooser.title = "Загрузить историю"
        story = fileChooser.showOpenDialog(this.currentWindow)
        if (story != null) {
            loadStory()
            restart.isDisable = false
            loadGame.isDisable = false
            saveGame.isDisable = false
        }
    }

    fun onRestartStory() {
        var enableNew = true
        if (!saved) {
            enableNew = false
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Начать с начала"
            alert.headerText = "Вы действительно хотите завершить текущую историю и начать новую?\r\nВедь всё, чего вы тут достигли - не сохраниться."
            alert.contentText = "Что будем делать?"

            val saveButtonType = ButtonType("Сохранить", ButtonBar.ButtonData.APPLY)
            val nextButtonType = ButtonType("Продолжить", ButtonBar.ButtonData.YES)
            val cancelButtonType = ButtonType("Прервать", ButtonBar.ButtonData.CANCEL_CLOSE)

            alert.buttonTypes.setAll(saveButtonType, nextButtonType, cancelButtonType)

            val result = alert.showAndWait()
            result.ifPresent { type ->
                when (type) {
                    ButtonType.APPLY -> {
                        onSaveGame()
                    }
                    ButtonType.YES -> {
                        enableNew = true
                    }
                    ButtonType.CANCEL -> {
                        // Nothing to do
                    }
                }
            }
        }
        if (enableNew) {
            outputPlainText("Начинаем заново...\n")
            engine.loadStory(story?.absolutePath, true)
            engine.initGame()
        }
    }

    fun onLoadGame() {
        val fileChooser = FileChooser()
        fileChooser.title = "Загрузить игру"
        val game = fileChooser.showOpenDialog(this.currentWindow)
        if (game != null) {
            if (game.exists()) {
                engine.loadGame(game.absolutePath)
                saved = true
            } else {
                outputPlainText("Выбранный файл не существует.")
            }
        }
    }

    fun onSaveGame() {
        val fileChooser = FileChooser()
        fileChooser.title = "Сохранить игру"
        val game = fileChooser.showSaveDialog(this.currentWindow)
        if (game != null) {
            engine.saveGame(game.absolutePath)
            saved = true
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

    private fun updateTitle(title: String) {
        TODO("Need to know how it possible change title of frame")
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
            saved = false
        }
    }

    override fun outputPlainText(text: String) {
        outPanel.add(Text(text))
    }

    override fun outputIcon(icon: Icon?) {
        TODO("not implemented")
        //TODO: Need convert icon to ImageView
    }

}
