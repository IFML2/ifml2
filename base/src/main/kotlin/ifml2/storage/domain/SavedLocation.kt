package ifml2.storage.domain

interface SavedLocation {
    var id: String
    var items: MutableList<String>
}