package com.example.bluetooth_print.print;

import android.graphics.Bitmap
import java.nio.charset.Charset

class Image(
    val x: Float,
    val y: Float,
    val width: Float,
    val image: Bitmap
) : PrintObject() {

    override val objectHeight: Int
        get() = 180

    override fun hasCpclRepresentation(): Boolean {
        return true
    }

    override fun hasPtpRepresentation(): Boolean {
        return true
    }

    override fun toCpcl(document: PrintDocument, currentHeight: Int): String {
        return CpclBitmapUtils.convertToCpclCommand(image, x.toInt(), currentHeight, width.toInt(), objectHeight)
    }

    override fun toPtp(charset: Charset, smallVersion: Boolean) {
        return PtpBitmapUtils.decodeBitmap(image, width.toInt(), objectHeight)
    }
}