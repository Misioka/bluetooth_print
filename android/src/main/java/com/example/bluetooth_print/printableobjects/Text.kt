package com.example.bluetooth_print;

import java.nio.charset.Charset

/**
 * @author tomas (tomas.biesok@astrumq.eu)
 */
class Text(
        val font: PrintDocument.Font,
        val text: String,
        val indent: Int = 0,
        val keepHeight: Boolean = false
) : PrintObject() {

    override val objectHeight: Int
        get() = if (keepHeight) 0 else font.charHeight

    val bold = byteArrayOf(27, 69, 1)
    val nobold = byteArrayOf(27, 69, 0)

    override fun toCpcl(document: PrintDocument, currentHeight: Int): String {
        return PrintDocument.CPCL_CMD_TEXT.format(font.fontName, 0, indent, currentHeight + font.vertOffset, text)
    }

    override fun hasCpclRepresentation(): Boolean {
        return true
    }

    override fun hasPtpRepresentation(): Boolean {
        return true
    }

    override fun toPtp(charset: Charset, smallVersion: Boolean): ByteArray {
        val textBytes = text.toByteArray(charset)

        return if (font == PrintDocument.Font.NORMAL) {
            arrayOf(nobold, textBytes).toBytes()
        }
        else {
            arrayOf(bold, textBytes).toBytes()
        }
    }

}