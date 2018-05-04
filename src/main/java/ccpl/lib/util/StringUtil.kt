package ccpl.lib.util

fun notPartOfNumber(input: String, text: String) : Boolean = !input.matches("(-|[0-9]|\\.)")
            || (input.matches("-") && text.isNotEmpty())
            || (input.matches("\\.") && text.contains("."))

private fun String.matches(regex: String): Boolean {
  return this.matches(Regex(regex))
}
