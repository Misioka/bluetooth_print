package com.example.bluetooth_print;

import android.graphics.Paint
import java.nio.charset.Charset

/**
 * @author tomas (tomas.biesok@astrumq.eu)
 */
class Alignment(
        val align: Paint.Align
) : PrintObject() {
    override val objectHeight: Int
        get() = 0

    override fun hasCpclRepresentation(): Boolean {
        return true
    }

    override fun hasPtpRepresentation(): Boolean {
        return true
    }

    override fun toCpcl(document: PrintDocument, currentHeight: Int): String {
        return when (align) {
            Paint.Align.LEFT -> PrintDocument.CPCL_CMD_LEFT
            Paint.Align.CENTER -> PrintDocument.CPCL_CMD_CENTER
            Paint.Align.RIGHT -> PrintDocument.CPCL_CMD_RIGHT
        }
    }

    override fun toPtp(charset: Charset, smallVersion: Boolean): ByteArray {
        return when (align) {
            Paint.Align.LEFT -> PrintDocument.PTP_ESC_ALIGN_LEFT
            Paint.Align.CENTER -> PrintDocument.PTP_ESC_ALIGN_CENTER
            Paint.Align.RIGHT -> PrintDocument.PTP_ESC_ALIGN_RIGHT
        }
    }
}