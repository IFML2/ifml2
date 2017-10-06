package ifml2.fx.player

import ifml2.fx.player.view.PlayerView
import ifml2.service.HistoryService
import ifml2.service.ServiceRegistry
import tornadofx.App

class FxPlayerApp : App() {
    override val primaryView = PlayerView::class
}