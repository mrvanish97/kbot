package io.github.mrvanish97.kbot.utils

import java.awt.Graphics2D

fun Graphics2D.drawMultilineString(str: String, x: Int, y: Int) {
  var lineY = y
  val fontHeight = fontMetrics.height
  str.split('\n').forEach {
    drawString(it, x, lineY)
    lineY += fontHeight
  }
}