package ifml2.service

interface Service {
    val name: String

    fun start()
    fun stop()
}