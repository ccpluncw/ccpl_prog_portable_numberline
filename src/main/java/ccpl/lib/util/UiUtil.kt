package ccpl.lib.util

import java.awt.Toolkit

private val tk = Toolkit.getDefaultToolkit()

fun screenWidth() : Int = tk.screenSize.width

fun screenHeight() : Int = tk.screenSize.height