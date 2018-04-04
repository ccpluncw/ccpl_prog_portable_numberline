package ccpl.lib.util

import ccpl.lib.Bundle
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URL
import kotlin.streams.toList

const val delimiter = ":"
const val commentCharacter = '#'

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

fun writeDbFile(bundle: Bundle, path: URL) {
  val file = File(path.toURI())
  file.parentFile.mkdirs()


  val pw = PrintWriter(FileOutputStream(file))
  pw.println(bundle.toString())
  pw.close()
}
