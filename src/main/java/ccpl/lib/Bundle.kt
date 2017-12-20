package ccpl.lib


class Bundle {
  private val values: MutableMap<String, Any> = hashMapOf()

  val size
    get() = values.size

  fun add(key: String, value: Any) {
    values.put(key, value)
  }

  private fun get(key: String): Any? {
    return if (!values.containsKey(key)) {
      throw Exception("Bundle does not contain key: " + key)
    } else {
      values[key]
    }
  }

  fun getAsString(key: String) = get(key) as String

  fun getAsInt(key: String) = if (get(key) is String) Integer.parseInt(getAsString(key))
  else get(key) as Int

  fun getAsBoolean(key: String): Boolean = if (get(key) is String) getAsString(key).toBoolean()
  else get(key) as Boolean

  fun merge(secondBundle: Bundle) : Bundle {
    val newBundle = Bundle()

    secondBundle.values.forEach { newBundle.add(it.key, it.value) }
    values.forEach { newBundle.add(it.key, it.value) }

    return newBundle
  }

  override fun toString(): String = values.map { it.key + ": " + it.value }.joinToString("\n")

  fun contains(key: String) = values.contains(key)
}