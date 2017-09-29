package ccpl.numberline

import java.io.*
import java.net.URL
import kotlin.streams.toList

val delimiter = ":"
val commentCharacter = '#'

fun readDbFile(path: URL): Bundle {
  val dbfileValues = Bundle()

  val bis = BufferedReader(InputStreamReader(path.openStream()))

  val linesToParse = bis.lines()
      .filter{ line -> line.isNotEmpty() }
      .filter { line -> line[0] != commentCharacter }
      .map { line -> line.split(commentCharacter)[0] }
      .toList()

  linesToParse.forEach { line ->
    val split = line.split(delimiter)
    dbfileValues.add(split[0], split[1].trim())
  }

  return dbfileValues
}