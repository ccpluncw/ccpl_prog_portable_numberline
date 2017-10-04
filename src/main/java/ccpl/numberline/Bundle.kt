package ccpl.numberline

class Bundle {
  private val values: MutableMap<String, Any> = hashMapOf()

  val size
    get() = values.size

  fun add(key: String, value: Any) {
    values.put(key, value)
  }

  fun get(key: String): Any? {
    return if (!values.containsKey(key)) {
      throw Exception("Bundle does not contain key: " + key)
    } else {
      values[key]
    }
  }

  fun getAsString(key: String) = get(key) as String

  fun getAsInt(key: String) = Integer.parseInt(getAsString(key))

}