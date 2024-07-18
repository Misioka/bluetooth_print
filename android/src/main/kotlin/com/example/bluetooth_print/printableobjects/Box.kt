package com.example.bluetooth_print.print;

/**
 * @author tomas (tomas.biesok@astrumq.eu)
 */
class Box(
        val w: Int,
        val h: Int,
        val offsetX: Int = 0,
        val thickness: Int = 1
) : PrintObject() {

    override val objectHeight: Int
        get() = height

    override fun hasCpclRepresentation(): Boolean {
        return true
    }

    override fun hasPtpRepresentation(): Boolean {
        return false
    }

    override fun toCpcl(document: PrintDocument, currentHeight: Int): String {
        return PrintDocument.CPCL_CMD_BOX.format(offsetX, currentHeight, offsetX + w, currentHeight + h, thickness)
    }
}