package com.example.bluetooth_print.print;

import java.nio.charset.Charset

/**
 * @author tomas (tomas.biesok@astrumq.eu)
 */
abstract class PrintObject {

    var marginTop: Int = 0
    var marginBottom: Int = 0

    val height: Int
        get() = marginTop + marginBottom + objectHeight

    fun setMargin(top: Int, bottom: Int) {
        this.marginTop = top
        this.marginBottom = bottom
    }

    abstract protected val objectHeight: Int

    abstract fun hasCpclRepresentation(): Boolean

    abstract fun hasPtpRepresentation(): Boolean

    open fun toCpcl(document: PrintDocument, currentHeight: Int): String {
        return ""
    }

    open fun toPtp(charset: Charset, smallVersion: Boolean): ByteArray {
        return "".toByteArray()
    }
}