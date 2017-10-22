package ifml2.storage.domain

interface SavedRole {
    var name: String
    var properties: MutableList<SavedProperty>
}