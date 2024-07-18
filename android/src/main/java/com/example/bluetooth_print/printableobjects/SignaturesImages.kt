package com.example.bluetooth_print;

import java.nio.charset.Charset

class SignaturesImages(
    val images: List<Image>
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
        return images.joinToString(separator = PrintDocument.NEWLINE) { it.toCpcl(document, currentHeight) }
    }

    override fun toPtp(charset: Charset, smallVersion: Boolean): ByteArray {
        if (images.size != 2) {
            return byteArrayOf()
        }

        val firstImage = images[0]
        val secondImage = images[1]

        return PtpBitmapUtils.decodeTwoBitmaps(
                firstImage.image,
                secondImage.image,
                firstImage.width.toInt(),
                objectHeight,
                firstImage.width.toInt() * 2,
                objectHeight
        )
    }
}