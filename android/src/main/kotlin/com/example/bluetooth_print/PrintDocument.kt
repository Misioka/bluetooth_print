package com.example.bluetooth_print.print;

import android.graphics.Bitmap
import android.graphics.Paint
import java.nio.charset.Charset
import java.util.*

class PrintDocument(
        val title: String,
        val printerType: PrinterType,
        val docWidth: Int = PrintDocument.DOCUMENT_3_INCH_WIDTH
) {

    val objects: MutableList<PrintObject> = ArrayList()

    var marginTop: Int = 10
    var marginBottom: Int = 96

    var lastAlignment: Paint.Align = Paint.Align.LEFT

    fun setMargin(top: Int, bottom: Int) {
        this.marginTop = top
        this.marginBottom = bottom
    }

    fun calculateHeight(): Int {
        return objects.map { it.height }.sum() + marginTop + marginBottom
    }

    fun generatePrinterCommands(quantity: Int = 1, printerType: PrinterType, smallVersion: Boolean = false): ByteArray {
        val charset = charset(printerType.charset)

        return if (printerType == PrinterType.ZEBRA) {
            generateZebraCommands(quantity).toByteArray(charset)
        } else {
            generatePtpCommands(charset, smallVersion)
        }
    }

    private fun generatePtpCommands(charset: Charset, smallVersion: Boolean): ByteArray {
        var bytes = byteArrayOf(0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39, 0x01)

        for (i in 0..1) {
            objects.add(Text(Font.NORMAL, NEWLINE))
        }

        objects.forEach {
            if (it.hasPtpRepresentation()) {
                bytes = arrayOf(bytes, it.toPtp(charset, smallVersion), NEWLINE.toByteArray(charset)).toBytes()
            }
        }

        return bytes
    }

    private fun generateZebraCommands(quantity: Int): String {
        val cpclCommands = StringBuilder()
        // add required ZEBRA header, offset is 0, resolution is 200 DPI
        cpclCommands.append(CPCL_HEADER.format(0, 200, 200, calculateHeight(), quantity))
                .append(NEWLINE)

        var currentHeight = marginTop
        objects.forEach {
            if (it.hasCpclRepresentation()) {
                cpclCommands.append(it.toCpcl(this, currentHeight))
                        .append(NEWLINE)
            }
            currentHeight += it.height

        }

        cpclCommands.append(CPCL_CMD_PRINT).append(NEWLINE)
        return cpclCommands.toString()
    }

    fun setAlignment(align: Paint.Align) {
        if (lastAlignment != align) {
            objects.add(Alignment(align))
        }
        lastAlignment = align
    }

    fun appendText(text: String, font: Font = Font.NORMAL, indent: Int = 0, align: Paint.Align = Paint.Align.LEFT) {
        if (text.isEmpty()) {
            return
        }

        var cleanedText = text.trim()

        if (cleanedText.length * font.charWidth > docWidth) {
            val maxChars = Math.floor(docWidth / font.charWidth.toDouble()).toInt()
            cleanedText = cleanedText.substring(0..(maxChars - 3)) + "..."
        }

        if (lastAlignment != align) {
            objects.add(Alignment(align))
        }

        objects.add(Text(font, cleanedText))

        lastAlignment = align
    }

    fun appendMultilineText(text: String, font: Font = Font.NORMAL, indent: Int = 0, align: Paint.Align = Paint.Align.LEFT) {
        if (text.isEmpty()) {
            return
        }

        val texts: MutableList<Text> = ArrayList()
        val charsPerLine = Math.floor(docWidth / font.charWidth.toDouble()).toInt() - indent
        val words = text.split("\\s".toRegex()).toMutableList()

        var text = ""

        while (words.size > 0) {
            val word = words.removeAt(0)

            if ((text + word).length >= charsPerLine) {
                texts.add(Text(font, text, indent))
                if (word.length < charsPerLine) {
                    text = word
                } else {
                    val wordParts = word.equallySplit(charsPerLine)
                    if (wordParts.size > 1) {
                        wordParts.subList(0, wordParts.lastIndex)
                                .mapTo(texts) { Text(font, it, indent) }
                    }
                    text = wordParts.last()
                }
            } else {
                if (text.isNotEmpty()) {
                    text += " "
                }
                text += word
            }
        }

        if (text.isNotEmpty()) {
            texts.add(Text(font, text, indent))
        }

        if (lastAlignment != align) {
            objects.add(Alignment(align))
        }

        objects.addAll(texts)

        lastAlignment = align
    }

    fun appendLine(margin: Int = Font.NORMAL.charHeight / 4) {
        val line = Line(2)
        line.setMargin(margin, margin)
        objects.add(line)
    }

    fun appendSpace(charSize: Int, lines: Int) {
        objects.add(Space(charSize, lines))
    }

    fun appendSpace(size: Int) {
        objects.add(Space(size))
    }

    fun appendEmptyLine() {
        appendSpace(Font.NORMAL.charHeight / 2)
    }

    fun appendCanonicalTableRow(columns: Array<String>, font: Font = Font.NORMAL) {
        val ratios = IntArray(columns.size)
        val rate: Int = if (printerType == PrinterType.ZEBRA) 1
        else 48 / columns.size

        Arrays.fill(ratios, rate)
        appendTableRow(columns, ratios, font)
    }

    fun appendCanonicalTableRow(columns: Array<String>, ratios: IntArray, font: Font = Font.NORMAL) {
        appendTableRow(columns, ratios, font)
    }

    fun appendTableRow(columns: Array<String>, ratioKey: String, font: Font) {
        appendTableRow(columns, printerType.ratios[ratioKey], font)
    }

    fun appendTableRow(columns: Array<String>, ratios: IntArray?, font: Font) {
        if (ratios == null || columns.isEmpty() || ratios.isEmpty() || columns.size != ratios.size) {
            throw IllegalArgumentException("columns and ratios must be non-empty arrays of the same size")
        }

        if (printerType == PrinterType.ZEBRA) {
            val indents = computeIndents(ratios, docWidth)
            indents.indices.mapTo(objects) { Text(font, columns[it], indents[it], it !== indents.size - 1) }

        } else {
            var text = ""

            for ((index, column) in columns.withIndex()) {
                text += String.format("%-" + ratios[index] + "s", column)
            }

            objects.add(Text(font, text, 0))
        }
    }

    fun appendSignature(signatureDriver: Bitmap, signatureCustomer: Bitmap) {
        val imageDriver = Image(0f, 0f, docWidth.toFloat() / 2, signatureDriver)
        val imageCustomer = Image(docWidth.toFloat() / 2, 0f, docWidth.toFloat() / 2, signatureCustomer)
        objects.add(SignaturesImages(listOf(imageDriver, imageCustomer)))
    }

    private fun computeIndents(ratios: IntArray, thickness: Int): IntArray {
        val sum = ratios
                .map(Int::toFloat)
                .sum()

        val indents = IntArray(ratios.size)
        indents[0] = 0
        for (i in 1..ratios.size - 1) {
            indents[i] = indents[i - 1] + Math.floor((thickness / sum * ratios[i - 1]).toDouble()).toInt()
        }
        return indents
    }


    enum class Font(
            val fontFile: String,
            val charWidth: Int,
            val charHeight: Int,
            val vertOffset: Int,
            val bold: Boolean = false
    ) {
        NORMAL(DOCUMENT_FONT_LM_10, 14, 24, -8),
        HEADLINE(DOCUMENT_FONT_LM_10_BOLD, 15, 24, -8, true),
        BIG(DOCUMENT_FONT_LM_12_BOLD, 19, 42, -8, true);

        val fontName: String
            get() = fontFile
    }

    enum class Type {
        BUYOUT, SELLOUT, PACKAGE, DEBT
    }

    companion object {
        const val DOCUMENT_2_INCH_WIDTH = 383
        const val DOCUMENT_2_25_INCH_WIDTH = 447
        const val DOCUMENT_3_INCH_WIDTH = 575
        const val DOCUMENT_4_INCH_WIDTH = 831

        const val DOCUMENT_FONT_LM_10 = "LM10pt.cpf"
        const val DOCUMENT_FONT_LM_10_BOLD = "LM10Bpt.cpf"
        const val DOCUMENT_FONT_LM_12_BOLD = "LM12Bpt.cpf"

        const val NEWLINE = "\r\n"
        /**
         * Required at top of each ZEBRA file
         *
         * Arguments are:
         * offset - The horizontal offset for the entire label. This value causes all fields to be offset horizontally
        by the specified number of UNITS.
         * hRes - Horizontal resolution (in dots-per-inch)
         * vRes - Vertical resolution (in dots-per-inch)
         * height - The maximum height of the label.
         * qty - Quantity of labels to be printed. Maximum = 1024
         */
        const val CPCL_HEADER = "! %s %s %s %s %s"

        /**
         * The PRINT command terminates and prints the file
         */
        const val CPCL_CMD_PRINT = "PRINT"

        /**
         * Center justifies all subsequent fields
         */
        const val CPCL_CMD_CENTER = "CENTER"

        /**
         * Left justifies all subsequent fields
         */
        const val CPCL_CMD_LEFT = "LEFT"

        /**
         * Right justifies all subsequent fields
         */
        const val CPCL_CMD_RIGHT = "RIGHT"

        /**
         * The TEXT command is used to place text on a label
         *
         * Arguments:
         * font - Name/number of the font
         * size - Size identifier for the font
         * x - Horizontal starting position
         * y - Vertical starting position
         * data - The text to be printed
         */
        const val CPCL_CMD_TEXT = "TEXT %s %d %d %d %s"

        /**
         * Lines of any length, thickness, and angular orientation can be drawn using the LINE command
         *
         * Arguments:
         * x0 - X-coordinate of the top-left corner
         * y0 - Y-coordinate of the top-left corner
         * x1 - X-coordinate of: top right corner for horizontal, bottom left corner for vertical
         * y1 - Y-coordinate of: top right corner for horizontal, bottom left corner for vertical
         * thickness - Unit-thickness (or thickness) of the line
         */
        const val CPCL_CMD_LINE = "LINE %d %d %d %d %d"

        /**
         * The BOX command provides the user with the ability to produce rectangular shapes of specified line
        thickness
         * x0 - X-coordinate of the top left corner
         * y0 - Y-coordinate of the top left corner
         * x1 - X-coordinate of the bottom right corner
         * y1 - Y-coordinate of the bottom right corner
         * thickness - Unit-width (or thickness) of the lines forming the box
         */
        const val CPCL_CMD_BOX = "BOX %d %d %d %d %d"

        /**
         * The graphic command used for print bitmap image
         *
         * Arguments:
         * byteWidth - size of bytes of first line in your data
         * height - the number of lines in your data
         * x - horizontal starting position
         * y - vertical starting position
         * data - the image data
         */
        const val CPCL_CMD_EG = "EG %d %d %d %d %s"

        /**
         * Align commands for PTP
         */
        val PTP_ESC_ALIGN_LEFT = byteArrayOf(0x1b, 'a'.toByte(), 0x00)
        val PTP_ESC_ALIGN_RIGHT = byteArrayOf(0x1b, 'a'.toByte(), 0x02)
        val PTP_ESC_ALIGN_CENTER = byteArrayOf(0x1b, 'a'.toByte(), 0x01)
    }

}