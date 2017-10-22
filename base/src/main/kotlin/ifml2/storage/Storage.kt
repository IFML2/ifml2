package ifml2.storage

import ifml2.IFML2Exception
import ifml2.storage.domain.SavedGame

interface Storage {
    @Throws(IFML2Exception::class)
    fun saveGame(fileName: String, savedGame: SavedGame)
    @Throws(IFML2Exception::class)
    fun loadGame(fileName: String): SavedGame
}