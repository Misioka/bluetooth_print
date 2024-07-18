package android.src.main.java.com.example.bluetooth_print

import java.nio.charset.Charset

/**
 * @author tomas (tomas.biesok@astrumq.eu)
 */
class Space(
        val charSize: Int,
        val lines: Int = 1
) : PrintObject() {

    override val objectHeight: Int
        get() = charSize * lines

    override fun hasCpclRepresentation(): Boolean {
        return false
    }

    override fun hasPtpRepresentation(): Boolean {
        return lines != 1
    }

    override fun toPtp(charset: Charset, smallVersion: Boolean): ByteArray {
        var textLines = ""
        for (i in 0..lines) {
            textLines += PrintDocument.NEWLINE
        }

        return textLines.toByteArray()
    }
}