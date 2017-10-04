package ifml2.fx.player

import ifml2.fx.player.view.PlayerView
import tornadofx.App

class FxPlayerApp : App() {
    override val primaryView = PlayerView::class
}