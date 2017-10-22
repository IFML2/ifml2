package ifml2.storage.domain

interface SavedItem {
    var id: String
    var roles: MutableList<SavedRole>
}