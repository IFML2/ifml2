package ifml2.service

class HistoryService() : Ifml2Service {
    override val name = "HistoryService"
    override fun start() {}
    override fun stop () {}
    private val history = arrayListOf<String>()
    private var historyIterator = history.listIterator()
    fun prev(): String = if (historyIterator.hasPrevious()) historyIterator.previous() else ""
    fun next(): String = if (historyIterator.hasNext()) historyIterator.next() else ""
    fun update(command: String) {
        history.add(command)
        historyIterator = history.listIterator(history.size)
    }
}