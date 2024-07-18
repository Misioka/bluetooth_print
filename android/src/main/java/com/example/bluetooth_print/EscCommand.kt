package android.src.main.java.com.example.bluetooth_print

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Locale
import java.util.Vector
import java.util.regex.Pattern

/**
 * author: Bill
 * created on: 17/11/24 下午4:47
 * description: 小票机指令集
 */
class EscCommand {
    var command: Vector<Byte?>? = null

    init {
        this.command = Vector<Any?>(4096, 1024)
    }

    private fun addArrayToCommand(array: ByteArray) {
        for (i in array.indices) {
            command!!.add(array[i])
        }
    }

    private fun addStrToCommand(str: String) {
        var bs: ByteArray? = null
        if (str != "") {
            try {
                bs = str.toByteArray(charset("GB2312"))
            } catch (var4: UnsupportedEncodingException) {
                var4.printStackTrace()
            }

            for (i in bs!!.indices) {
                command!!.add(bs[i])
            }
        }
    }

    private fun addStrToCommand(str: String, charset: Charset) {
        var bs: ByteArray? = null
        Log.d("EscCommand", "str" + str + "charset:" + charset)
        if (str != "") {
            try {
                bs = str.toByteArray(charset)
                Log.d(
                    "EscCommand",
                    "bs.length" + bs!!.size + "bytes" + bs.contentToString() + "charset:" + charset
                )
            } catch (var5: UnsupportedEncodingException) {
                var5.printStackTrace()
            }

            for (i in bs!!.indices) {
                command!!.add(bs[i])
            }
        }
    }

    private fun addStrToCommandUTF8Encoding(str: String, length: Int) {
        var length = length
        var bs: ByteArray? = null
        if (str != "") {
            try {
                bs = str.toByteArray(charset("UTF-8"))
            } catch (var5: UnsupportedEncodingException) {
                var5.printStackTrace()
            }

            Log.d("EscCommand", "bs.length" + bs!!.size)
            if (length > bs.size) {
                length = bs.size
            }

            Log.d("EscCommand", "length$length")

            for (i in 0 until length) {
                command!!.add(bs[i])
            }
        }
    }

    private fun addStrToCommand(str: String, length: Int) {
        var length = length
        var bs: ByteArray? = null
        if (str != "") {
            try {
                bs = str.toByteArray(charset("GB2312"))
            } catch (var5: UnsupportedEncodingException) {
                var5.printStackTrace()
            }

            Log.d("EscCommand", "bs.length" + bs!!.size)
            if (length > bs.size) {
                length = bs.size
            }

            Log.d("EscCommand", "length$length")

            for (i in 0 until length) {
                command!!.add(bs[i])
            }
        }
    }

    fun addHorTab() {
        val command = byteArrayOf(9)
        this.addArrayToCommand(command)
    }

    fun addText(text: String) {
        this.addStrToCommand(text)
    }

    fun addText(text: String, charsetName: String?) {
        this.addStrToCommand(text, charset(charsetName!!))
    }

    fun addArabicText(text: String) {
        var text = text
        text = GpUtils.reverseLetterAndNumber(text)
        text = GpUtils.splitArabic(text)
        val fooInput = text.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val var6 = fooInput
        val var5 = fooInput.size

        for (var4 in 0 until var5) {
            val `in` = var6[var4]
            val output = GpUtils.string2Cp864(`in`)

            for (i in output.indices) {
                if (output[i].toInt() == -16) {
                    this.addArrayToCommand(byteArrayOf(27, 116, 29, -124, 27, 116, 22))
                } else {
                    command!!.add(output[i])
                }
            }
        }
    }

    fun addPrintAndLineFeed() {
        val command = byteArrayOf(10)
        this.addArrayToCommand(command)
    }

    fun RealtimeStatusTransmission(status: STATUS) {
        val command = byteArrayOf(16, 4, status.getValue())
        this.addArrayToCommand(command)
    }

    fun addGeneratePluseAtRealtime(foot: LabelCommand.FOOT, t: Byte) {
        var t = t
        val command = byteArrayOf(16, 20, 1, foot.value.toByte(), 0)
        if (t > 8) {
            t = 8
        }

        command[4] = t
        this.addArrayToCommand(command)
    }

    fun addSound(n: Byte, t: Byte) {
        var n = n
        var t = t
        val command = byteArrayOf(27, 66, 0, 0)
        if (n < 0) {
            n = 1
        } else if (n > 9) {
            n = 9
        }

        if (t < 0) {
            t = 1
        } else if (t > 9) {
            t = 9
        }

        command[2] = n
        command[3] = t
        this.addArrayToCommand(command)
    }

    fun addSetRightSideCharacterSpacing(n: Byte) {
        val command = byteArrayOf(27, 32, n)
        this.addArrayToCommand(command)
    }

    fun addSelectPrintModes(
        font: FONT,
        emphasized: ENABLE,
        doubleheight: ENABLE,
        doublewidth: ENABLE,
        underline: ENABLE
    ) {
        var temp: Byte = 0
        if (font == FONT.FONTB) {
            temp = 1
        }

        if (emphasized == ENABLE.ON) {
            temp = (temp.toInt() or 8).toByte()
        }

        if (doubleheight == ENABLE.ON) {
            temp = (temp.toInt() or 16).toByte()
        }

        if (doublewidth == ENABLE.ON) {
            temp = (temp.toInt() or 32).toByte()
        }

        if (underline == ENABLE.ON) {
            temp = (temp.toInt() or 128).toByte()
        }

        val command = byteArrayOf(27, 33, temp)
        this.addArrayToCommand(command)
    }

    fun addSetAbsolutePrintPosition(n: Short) {
        val command = byteArrayOf(27, 36, 0, 0)
        val nl = (n % 256).toByte()
        val nh = (n / 256).toByte()
        command[2] = nl
        command[3] = nh
        this.addArrayToCommand(command)
    }

    fun addSelectOrCancelUserDefineCharacter(enable: ENABLE) {
        val command = byteArrayOf(27, 37, 0)
        if (enable == ENABLE.ON) {
            command[2] = 1
        } else {
            command[2] = 0
        }

        this.addArrayToCommand(command)
    }

    fun addTurnUnderlineModeOnOrOff(underline: UNDERLINE_MODE) {
        val command = byteArrayOf(27, 45, underline.getValue())
        this.addArrayToCommand(command)
    }

    fun addSelectDefualtLineSpacing() {
        val command = byteArrayOf(27, 50)
        this.addArrayToCommand(command)
    }

    fun addSetLineSpacing(n: Byte) {
        val command = byteArrayOf(27, 51, n)
        this.addArrayToCommand(command)
    }

    fun addCancelUserDefinedCharacters(n: Byte) {
        val command = byteArrayOf(27, 63, 0)
        if (n >= 32 && n <= 126) {
            command[2] = n
        } else {
            command[2] = 32
        }

        this.addArrayToCommand(command)
    }

    fun addInitializePrinter() {
        val command = byteArrayOf(27, 64)
        this.addArrayToCommand(command)
    }

    fun addTurnEmphasizedModeOnOrOff(enabel: ENABLE) {
        val command = byteArrayOf(27, 69, enabel.getValue())
        this.addArrayToCommand(command)
    }

    fun addTurnDoubleStrikeOnOrOff(enabel: ENABLE) {
        val command = byteArrayOf(27, 71, enabel.getValue())
        this.addArrayToCommand(command)
    }

    fun addPrintAndFeedPaper(n: Byte) {
        val command = byteArrayOf(27, 74, n)
        this.addArrayToCommand(command)
    }

    fun addSelectCharacterFont(font: FONT) {
        val command = byteArrayOf(27, 77, font.getValue())
        this.addArrayToCommand(command)
    }

    fun addSelectInternationalCharacterSet(set: CHARACTER_SET) {
        val command = byteArrayOf(27, 82, set.getValue())
        this.addArrayToCommand(command)
    }

    fun addTurn90ClockWiseRotatin(enabel: ENABLE) {
        val command = byteArrayOf(27, 86, enabel.getValue())
        this.addArrayToCommand(command)
    }

    fun addSetRelativePrintPositon(n: Short) {
        val command = byteArrayOf(27, 92, 0, 0)
        val nl = (n % 256).toByte()
        val nh = (n / 256).toByte()
        command[2] = nl
        command[3] = nh
        this.addArrayToCommand(command)
    }

    fun addSelectJustification(just: JUSTIFICATION) {
        val command = byteArrayOf(27, 97, just.getValue())
        this.addArrayToCommand(command)
    }

    fun addPrintAndFeedLines(n: Byte) {
        val command = byteArrayOf(27, 100, n)
        this.addArrayToCommand(command)
    }

    fun addGeneratePlus(foot: LabelCommand.FOOT, t1: Byte, t2: Byte) {
        val command = byteArrayOf(27, 112, foot.value.toByte(), t1, t2)
        this.addArrayToCommand(command)
    }

    fun addSelectCodePage(page: CODEPAGE) {
        val command = byteArrayOf(27, 116, page.getValue())
        this.addArrayToCommand(command)
    }

    fun addTurnUpsideDownModeOnOrOff(enable: ENABLE) {
        val command = byteArrayOf(27, 123, enable.getValue())
        this.addArrayToCommand(command)
    }

    fun addSetCharcterSize(width: WIDTH_ZOOM, height: HEIGHT_ZOOM) {
        val command = byteArrayOf(29, 33, 0)
        var temp: Byte = 0
        temp = (temp.toInt() or width.getValue().toInt()).toByte()
        temp = (temp.toInt() or height.getValue().toInt()).toByte()
        command[2] = temp
        this.addArrayToCommand(command)
    }

    fun addTurnReverseModeOnOrOff(enable: ENABLE) {
        val command = byteArrayOf(29, 66, enable.getValue())
        this.addArrayToCommand(command)
    }

    fun addSelectPrintingPositionForHRICharacters(position: HRI_POSITION) {
        val command = byteArrayOf(29, 72, position.getValue())
        this.addArrayToCommand(command)
    }

    fun addSetLeftMargin(n: Short) {
        val command = byteArrayOf(29, 76, 0, 0)
        val nl = (n % 256).toByte()
        val nh = (n / 256).toByte()
        command[2] = nl
        command[3] = nh
        this.addArrayToCommand(command)
    }

    fun addSetHorAndVerMotionUnits(x: Byte, y: Byte) {
        val command = byteArrayOf(29, 80, x, y)
        this.addArrayToCommand(command)
    }

    fun addCutAndFeedPaper(length: Byte) {
        val command = byteArrayOf(29, 86, 66, length)
        this.addArrayToCommand(command)
    }

    fun addCutPaper() {
        val command = byteArrayOf(29, 86, 1)
        this.addArrayToCommand(command)
    }

    fun addSetPrintingAreaWidth(width: Short) {
        val nl = (width % 256).toByte()
        val nh = (width / 256).toByte()
        val command = byteArrayOf(29, 87, nl, nh)
        this.addArrayToCommand(command)
    }

    fun addSetAutoSatusBack(enable: ENABLE) {
        val command = byteArrayOf(29, 97, 0)
        if (enable == ENABLE.OFF) {
            command[2] = 0
        } else {
            command[2] = -1
        }

        this.addArrayToCommand(command)
    }

    fun addSetFontForHRICharacter(font: FONT) {
        val command = byteArrayOf(29, 102, font.getValue())
        this.addArrayToCommand(command)
    }

    fun addSetBarcodeHeight(height: Byte) {
        val command = byteArrayOf(29, 104, height)
        this.addArrayToCommand(command)
    }

    fun addSetBarcodeWidth(width: Byte) {
        var width = width
        val command = byteArrayOf(29, 119, 0)
        if (width > 6) {
            width = 6
        }

        if (width < 2) {
            width = 1
        }

        command[2] = width
        this.addArrayToCommand(command)
    }

    fun addSetKanjiFontMode(DoubleWidth: ENABLE, DoubleHeight: ENABLE, Underline: ENABLE) {
        val command = byteArrayOf(28, 33, 0)
        var temp: Byte = 0
        if (DoubleWidth == ENABLE.ON) {
            temp = (temp.toInt() or 4).toByte()
        }

        if (DoubleHeight == ENABLE.ON) {
            temp = (temp.toInt() or 8).toByte()
        }

        if (Underline == ENABLE.ON) {
            temp = (temp.toInt() or 128).toByte()
        }

        command[2] = temp
        this.addArrayToCommand(command)
    }

    fun addSelectKanjiMode() {
        val command = byteArrayOf(28, 38)
        this.addArrayToCommand(command)
    }

    fun addSetKanjiUnderLine(underline: UNDERLINE_MODE) {
        val command = byteArrayOf(28, 45, 0)
        command[3] = underline.getValue()
        this.addArrayToCommand(command)
    }

    fun addCancelKanjiMode() {
        val command = byteArrayOf(28, 46)
        this.addArrayToCommand(command)
    }

    fun addSetKanjiLefttandRightSpace(left: Byte, right: Byte) {
        val command = byteArrayOf(28, 83, left, right)
        this.addArrayToCommand(command)
    }

    fun addSetQuadrupleModeForKanji(enable: ENABLE) {
        val command = byteArrayOf(28, 87, enable.getValue())
        this.addArrayToCommand(command)
    }

    fun addRastBitImage(bitmap: Bitmap?, nWidth: Int, nMode: Int) {
        if (bitmap != null) {
            val width = (nWidth + 7) / 8 * 8
            var height: Int = bitmap.getHeight() * width / bitmap.getWidth()
            val grayBitmap: Bitmap = GpUtils.toGrayscale(bitmap)
            val rszBitmap: Bitmap = GpUtils.resizeImage(grayBitmap, width, height)
            val src = GpUtils.bitmapToBWPix(rszBitmap)
            val command = ByteArray(8)
            height = src.size / width
            command[0] = 29
            command[1] = 118
            command[2] = 48
            command[3] = (nMode and 1).toByte()
            command[4] = (width / 8 % 256).toByte()
            command[5] = (width / 8 / 256).toByte()
            command[6] = (height % 256).toByte()
            command[7] = (height / 256).toByte()
            this.addArrayToCommand(command)
            val codecontent = GpUtils.pixToEscRastBitImageCmd(src)

            for (k in codecontent.indices) {
                this.command!!.add(codecontent[k])
            }
        } else {
            Log.d("BMP", "bmp.  null ")
        }
    }

    fun addDownloadNvBitImage(bitmap: Array<Bitmap>?) {
        if (bitmap == null) {
            Log.d("BMP", "bmp.  null ")
        } else {
            Log.d("BMP", "bitmap.length " + bitmap.size)
            val n = bitmap.size
            if (n > 0) {
                val command = byteArrayOf(28, 113, n.toByte())
                this.addArrayToCommand(command)

                for (i in 0 until n) {
                    var height: Int = (bitmap[i].getHeight() + 7) / 8 * 8
                    val width: Int = bitmap[i].getWidth() * height / bitmap[i].getHeight()
                    val grayBitmap: Bitmap = GpUtils.toGrayscale(bitmap[i])
                    val rszBitmap: Bitmap = GpUtils.resizeImage(grayBitmap, width, height)
                    val src = GpUtils.bitmapToBWPix(rszBitmap)
                    height = src.size / width
                    Log.d("BMP", "bmp  Width $width")
                    Log.d("BMP", "bmp  height $height")
                    val codecontent = GpUtils.pixToEscNvBitImageCmd(src, width, height)

                    for (k in codecontent.indices) {
                        this.command!!.add(codecontent[k])
                    }
                }
            }
        }
    }

    fun addPrintNvBitmap(n: Byte, mode: Byte) {
        val command = byteArrayOf(28, 112, n, mode)
        this.addArrayToCommand(command)
    }

    fun addUPCA(content: String) {
        val command = byteArrayOf(29, 107, 65, 11)
        if (content.length >= command[3]) {
            this.addArrayToCommand(command)
            this.addStrToCommand(content, 11)
        }
    }

    fun addUPCE(content: String) {
        val command = byteArrayOf(29, 107, 66, 11)
        if (content.length >= command[3]) {
            this.addArrayToCommand(command)
            this.addStrToCommand(content, command[3].toInt())
        }
    }

    fun addEAN13(content: String) {
        val command = byteArrayOf(29, 107, 67, 12)
        if (content.length >= command[3]) {
            this.addArrayToCommand(command)
            Log.d("EscCommand", "content.length" + content.length)
            this.addStrToCommand(content, command[3].toInt())
        }
    }

    fun addEAN8(content: String) {
        val command = byteArrayOf(29, 107, 68, 7)
        if (content.length >= command[3]) {
            this.addArrayToCommand(command)
            this.addStrToCommand(content, command[3].toInt())
        }
    }

    @SuppressLint(["DefaultLocale"])
    fun addCODE39(content: String) {
        var content = content
        val command = byteArrayOf(29, 107, 69, content.length.toByte())
        content = content.uppercase(Locale.getDefault())
        this.addArrayToCommand(command)
        this.addStrToCommand(content, command[3].toInt())
    }

    fun addITF(content: String) {
        val command = byteArrayOf(29, 107, 70, content.length.toByte())
        this.addArrayToCommand(command)
        this.addStrToCommand(content, command[3].toInt())
    }

    fun addCODABAR(content: String) {
        val command = byteArrayOf(29, 107, 71, content.length.toByte())
        this.addArrayToCommand(command)
        this.addStrToCommand(content, command[3].toInt())
    }

    fun addCODE93(content: String) {
        val command = byteArrayOf(29, 107, 72, content.length.toByte())
        this.addArrayToCommand(command)
        this.addStrToCommand(content, command[3].toInt())
    }

    fun addCODE128(content: String) {
        val command = byteArrayOf(29, 107, 73, content.length.toByte())
        this.addArrayToCommand(command)
        this.addStrToCommand(content, command[3].toInt())
    }

    fun genCodeC(content: String): String {
        val bytes: MutableList<Byte?> = ArrayList<Any?>(20)
        val len = content.length
        bytes.add(123.toByte())
        bytes.add(67.toByte())

        run {
            var i = 0
            while (i < len) {
                i = (content[i].code - 48) * 10
                val bits = content[i + 1].code - 48
                val current = i + bits
                bytes.add(current.toByte())
                i += 2
            }
        }

        val bb = ByteArray(bytes.size)

        var i: Int
        i = 0
        while (i < bb.size) {
            bb[i] = (bytes[i]!!)
            ++i
        }

        return String(bb, 0, bb.size)
    }

    fun genCodeB(content: String): String {
        return String.format("{B%s", *arrayOf<Any>(content))
    }

    fun genCode128(content: String): String {
        val regex = "([^0-9])"
        val str = content.split(regex.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(content)
        var splitString: String? = null
        val strlen = str.size
        if (strlen > 0 && matcher.find()) {
            splitString = matcher.group(0)
        }

        val sb = StringBuilder()

        for (i in 0 until strlen) {
            val first = str[i]
            val len = first.length
            val result = len % 2
            if (result == 0) {
                val codeC = this.genCodeC(first)
                sb.append(codeC)
            } else {
                sb.append(this.genCodeB(first[0].toString()))
                sb.append(this.genCodeC(first.substring(1, first.length)))
            }

            if (splitString != null) {
                sb.append(this.genCodeB(splitString))
                splitString = null
            }
        }

        return sb.toString()
    }

    fun addSelectSizeOfModuleForQRCode(n: Byte) {
        val command = byteArrayOf(29, 40, 107, 3, 0, 49, 67, 3)
        command[7] = n
        this.addArrayToCommand(command)
    }

    fun addSelectErrorCorrectionLevelForQRCode(n: Byte) {
        val command = byteArrayOf(29, 40, 107, 3, 0, 49, 69, n)
        this.addArrayToCommand(command)
    }

    fun addStoreQRCodeData(content: String) {
        val command = byteArrayOf(
            29, 40, 107,
            ((content.toByteArray().size + 3) % 256).toByte(),
            ((content.toByteArray().size + 3) / 256).toByte(), 49, 80, 48
        )
        this.addArrayToCommand(command)
        var bs: ByteArray? = null
        if (content != "") {
            try {
                bs = content.toByteArray(charset("utf-8"))
            } catch (var5: UnsupportedEncodingException) {
                var5.printStackTrace()
            }

            for (i in bs!!.indices) {
                this.command!!.add(bs[i])
            }
        }
    }

    fun addPrintQRCode() {
        val command = byteArrayOf(29, 40, 107, 3, 0, 49, 81, 48)
        this.addArrayToCommand(command)
    }

    fun addUserCommand(command: ByteArray) {
        this.addArrayToCommand(command)
    }

    enum class CHARACTER_SET(private val value: Int) {
        USA(0),
        FRANCE(1),
        GERMANY(2),
        UK(3),
        DENMARK_I(4),
        SWEDEN(5),
        ITALY(6),
        SPAIN_I(7),
        JAPAN(8),
        NORWAY(9),
        DENMARK_II(10),
        SPAIN_II(11),
        LATIN_AMERCIA(12),
        KOREAN(13),
        SLOVENIA(14),
        CHINA(15);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class CODEPAGE(private val value: Int) {
        PC437(0),
        KATAKANA(1),
        PC850(2),
        PC860(3),
        PC863(4),
        PC865(5),
        WEST_EUROPE(6),
        GREEK(7),
        HEBREW(8),
        EAST_EUROPE(9),
        IRAN(10),
        WPC1252(16),
        PC866(17),
        PC852(18),
        PC858(19),
        IRANII(20),
        LATVIAN(21),
        ARABIC(22),
        PT151(23),
        PC747(24),
        WPC1257(25),
        VIETNAM(27),
        PC864(28),
        PC1001(29),
        UYGUR(30),
        THAI(255);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class ENABLE(private val value: Int) {
        OFF(0),
        ON(1);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class FONT(private val value: Int) {
        FONTA(0),
        FONTB(1);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class HEIGHT_ZOOM(private val value: Int) {
        MUL_1(0),
        MUL_2(1),
        MUL_3(2),
        MUL_4(3),
        MUL_5(4),
        MUL_6(5),
        MUL_7(6),
        MUL_8(7);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class HRI_POSITION(private val value: Int) {
        NO_PRINT(0),
        ABOVE(1),
        BELOW(2),
        ABOVE_AND_BELOW(3);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class JUSTIFICATION(private val value: Int) {
        LEFT(0),
        CENTER(1),
        RIGHT(2);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class STATUS(private val value: Int) {
        PRINTER_STATUS(1),
        PRINTER_OFFLINE(2),
        PRINTER_ERROR(3),
        PRINTER_PAPER(4);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class UNDERLINE_MODE(private val value: Int) {
        OFF(0),
        UNDERLINE_1DOT(1),
        UNDERLINE_2DOT(2);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    enum class WIDTH_ZOOM(private val value: Int) {
        MUL_1(0),
        MUL_2(16),
        MUL_3(32),
        MUL_4(48),
        MUL_5(64),
        MUL_6(80),
        MUL_7(96),
        MUL_8(112);

        fun getValue(): Byte {
            return value.toByte()
        }
    }

    companion object {
        private const val DEBUG_TAG = "EscCommand"
        @JvmStatic
        fun main(args: Array<String>) {
            val escCommand = EscCommand()
            println(escCommand.genCodeC("123456"))
            println(escCommand.genCode128("123456-1234"))
        }
    }
}