package ifml2.engine

class TypedMap<E>(
        private val typedMap: MutableMap<String, E> = hashMapOf()
) {
    fun preSet(key: String, value: E): TypedMap<E> {
        put(key, value)
        return this
    }
    fun containsKey(key: String): Boolean = typedMap.containsKey(key.toLowerCase())
    fun put(key: String, value: E) { typedMap[key.toLowerCase()] = value }
    fun clear() { typedMap.clear() }
    fun get(key: String): E? = typedMap[key.toLowerCase()]
    fun getOrDefault(key: String, default: E): E = typedMap[key.toLowerCase()] ?: default
    fun keySet() = typedMap.keys
    fun entrySet() = typedMap.entries
}