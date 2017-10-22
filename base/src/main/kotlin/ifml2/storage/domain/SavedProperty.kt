package ifml2.storage.domain

interface SavedProperty {
    var name: String
    var items: MutableList<String>
}