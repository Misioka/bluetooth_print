package com.example.bluetooth_print;

import java.nio.charset.Charset

/**
 * @author tomas (tomas.biesok@astrumq.eu)
 */
class Line(
        val thickness: Int = 1
) : PrintObject() {
    override val objectHeight: Int
        get() = thickness

    override fun toCpcl(document: PrintDocument, currentHeight: Int): String {
        return PrintDocument.CPCL_CMD_LINE.format(0, currentHeight, document.docWidth, currentHeight, thickness)
    }

    override fun hasCpclRepresentation(): Boolean {
        return true
    }

    override fun hasPtpRepresentation(): Boolean {
        return true
    }

    override fun toPtp(charset: Charset, smallVersion: Boolean): ByteArray {
        return if (smallVersion)
            "________________________________".toByteArray(charset)
        else
            "________________________________________________".toByteArray(charset)
    }

}