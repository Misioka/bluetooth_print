package com.example.bluetooth_print;

import android.graphics.Bitmap
import android.graphics.Color

object CpclBitmapUtils {
    
    fun convertToCpclCommand(bitmap: Bitmap, xPos: Int, yPos: Int, width: Int, height: Int): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

        //Make sure the width is divisible by 8
        val dataString = StringBuilder()
        var loopWidth: Int = 8 - width % 8
        if (loopWidth == 8) loopWidth = width else loopWidth += width
        dataString.append("EG ${loopWidth / 8} $height $xPos $yPos ")
        for (y in 0 until height) {
            var bit = 128
            var currentValue = 0
            for (x in 0 until loopWidth) {
                var intensity: Int
                if (x < width) {
                    val color: Int = scaledBitmap.getPixel(x, y)
                    intensity = 255 - (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
                } else {
                    intensity = 0
                }
                if (intensity >= 128) {
                    currentValue = currentValue or bit
                }
                bit = bit shr 1
                if (bit == 0) {
                    dataString.append("%02x".format(currentValue))
                    bit = 128
                    currentValue = 0
                }
            } //x
        } //y
        dataString.append("\r\n")
        return dataString.toString()
    }
}