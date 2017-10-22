package ifml2.storage.domain

interface SavedGame {
    var storyFileName: String
    var globalVars: MutableList<SavedVariable>
    var systemVars: MutableList<SavedVariable>
    var savedInventory: MutableList<String>
    var savedLocations: MutableList<SavedLocation>
    var savedItems: MutableList<SavedItem>
}