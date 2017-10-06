package ifml2.service

class HistoryService() : Service {

    override val name: String = "HistoryService"

    override fun start() {}
    override fun stop () {}

    val history = arrayListOf<String>()
    var historyIterator = history.listIterator()

    fun prev(): String = if (historyIterator.hasPrevious()) historyIterator.previous() else ""

    fun next(): String = if (historyIterator.hasNext()) historyIterator.next() else ""

    fun update(command: String) {
        history.add(command)
        historyIterator = history.listIterator(history.size)
    }

}