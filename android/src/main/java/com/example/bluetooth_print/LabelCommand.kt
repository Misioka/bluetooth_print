package android.src.main.java.com.example.bluetooth_print

import android.graphics.Bitmap
import android.src.main.java.com.example.bluetooth_print.EscCommand.ENABLE
import android.util.Log
import java.io.UnsupportedEncodingException
import java.util.Vector

/**
 * author: Bill
 * created on: 17/11/24 下午4:47
 * description: 标签机指令集
 */
class LabelCommand {
    var command: Vector<Byte?>? = null

    constructor() {
        this.command = Vector<Byte?>()
    }

    constructor(width: Int, height: Int, gap: Int) {
        this.command = Vector<Byte?>(4096, 1024)
        this.addSize(width, height)
        this.addGap(gap)
    }

    fun clrCommand() {
        command!!.clear()
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

    fun addGap(gap: Int) {
        ""
        val str = "GAP $gap mm,0 mm\r\n"
        this.addStrToCommand(str)
    }

    fun addSize(width: Int, height: Int) {
        ""
        val str = "SIZE $width mm,$height mm\r\n"
        this.addStrToCommand(str)
    }

    fun addCashdrwer(m: FOOT, t1: Int, t2: Int) {
        ""
        val str = "CASHDRAWER " + m.value + "," + t1 + "," + t2 + "\r\n"
        this.addStrToCommand(str)
    }

    fun addOffset(offset: Int) {
        ""
        val str = "OFFSET $offset mm\r\n"
        this.addStrToCommand(str)
    }

    fun addSpeed(speed: SPEED) {
        ""
        val str = "SPEED " + speed.value + "\r\n"
        this.addStrToCommand(str)
    }

    fun addDensity(density: DENSITY) {
        ""
        val str = "DENSITY " + density.value + "\r\n"
        this.addStrToCommand(str)
    }

    fun addDirection(direction: DIRECTION, mirror: MIRROR) {
        ""
        val str = "DIRECTION " + direction.value + ',' + mirror.value + "\r\n"
        this.addStrToCommand(str)
    }

    fun addReference(x: Int, y: Int) {
        ""
        val str = "REFERENCE $x,$y\r\n"
        this.addStrToCommand(str)
    }

    fun addShif(shift: Int) {
        ""
        val str = "SHIFT $shift\r\n"
        this.addStrToCommand(str)
    }

    fun addCls() {
        ""
        val str = "CLS\r\n"
        this.addStrToCommand(str)
    }

    fun addFeed(dot: Int) {
        ""
        val str = "FEED $dot\r\n"
        this.addStrToCommand(str)
    }

    fun addBackFeed(dot: Int) {
        ""
        val str = "BACKFEED $dot\r\n"
        this.addStrToCommand(str)
    }

    fun addFormFeed() {
        ""
        val str = "FORMFEED\r\n"
        this.addStrToCommand(str)
    }

    fun addHome() {
        ""
        val str = "HOME\r\n"
        this.addStrToCommand(str)
    }

    fun addPrint(m: Int, n: Int) {
        ""
        val str = "PRINT $m,$n\r\n"
        this.addStrToCommand(str)
    }

    fun addPrint(m: Int) {
        ""
        val str = "PRINT $m\r\n"
        this.addStrToCommand(str)
    }

    fun addCodePage(page: CODEPAGE) {
        ""
        val str = "CODEPAGE " + page.value + "\r\n"
        this.addStrToCommand(str)
    }

    fun addSound(level: Int, interval: Int) {
        ""
        val str = "SOUND $level,$interval\r\n"
        this.addStrToCommand(str)
    }

    fun addLimitFeed(n: Int) {
        ""
        val str = "LIMITFEED $n\r\n"
        this.addStrToCommand(str)
    }

    fun addSelfTest() {
        ""
        val str = "SELFTEST\r\n"
        this.addStrToCommand(str)
    }

    fun addBar(x: Int, y: Int, width: Int, height: Int) {
        ""
        val str = "BAR $x,$y,$width,$height\r\n"
        this.addStrToCommand(str)
    }

    fun addText(
        x: Int,
        y: Int,
        font: FONTTYPE,
        rotation: ROTATION,
        Xscal: FONTMUL,
        Yscal: FONTMUL,
        text: String
    ) {
        ""
        val str =
            "TEXT " + x + "," + y + "," + "\"" + font.value + "\"" + "," + rotation.value + "," + Xscal.value + "," + Yscal.value + "," + "\"" + text + "\"" + "\r\n"
        this.addStrToCommand(str)
    }

    fun add1DBarcode(
        x: Int,
        y: Int,
        type: BARCODETYPE,
        height: Int,
        readable: READABEL,
        rotation: ROTATION,
        content: String
    ) {
        val narrow = 2
        val width = 2
        ""
        val str =
            "BARCODE " + x + "," + y + "," + "\"" + type.value + "\"" + "," + height + "," + readable.value + "," + rotation.value + "," + narrow + "," + width + "," + "\"" + content + "\"" + "\r\n"
        this.addStrToCommand(str)
    }

    fun add1DBarcode(
        x: Int,
        y: Int,
        type: BARCODETYPE,
        height: Int,
        readable: READABEL,
        rotation: ROTATION,
        narrow: Int,
        width: Int,
        content: String
    ) {
        val str =
            "BARCODE " + x + "," + y + "," + "\"" + type.value + "\"" + "," + height + "," + readable.value + "," + rotation.value + "," + narrow + "," + width + "," + "\"" + content + "\"" + "\r\n"
        this.addStrToCommand(str)
    }

    fun addBox(x: Int, y: Int, xend: Int, yend: Int, thickness: Int) {
        ""
        val str = "BOX $x,$y,$xend,$yend,$thickness\r\n"
        this.addStrToCommand(str)
    }

    fun addBitmap(x: Int, y: Int, mode: BITMAP_MODE, nWidth: Int, b: Bitmap?) {
        if (b != null) {
            var width = (nWidth + 7) / 8 * 8
            var height: Int = b.getHeight() * width / b.getWidth()
            Log.d("BMP", "bmp.getWidth() " + b.getWidth())
            val grayBitmap: Bitmap = GpUtils.toGrayscale(b)
            val rszBitmap: Bitmap = GpUtils.resizeImage(grayBitmap, width, height)
            val src = GpUtils.bitmapToBWPix(rszBitmap)
            height = src.size / width
            width /= 8
            val str = "BITMAP " + x + "," + y + "," + width + "," + height + "," + mode.value + ","
            this.addStrToCommand(str)
            val codecontent = GpUtils.pixToLabelCmd(src)

            for (k in codecontent.indices) {
                command!!.add(codecontent[k])
            }

            Log.d("LabelCommand", "codecontent$codecontent")
        }
    }

    fun addErase(x: Int, y: Int, xwidth: Int, yheight: Int) {
        ""
        val str = "ERASE $x,$y,$xwidth,$yheight\r\n"
        this.addStrToCommand(str)
    }

    fun addReverse(x: Int, y: Int, xwidth: Int, yheight: Int) {
        ""
        val str = "REVERSE $x,$y,$xwidth,$yheight\r\n"
        this.addStrToCommand(str)
    }

    fun addQRCode(x: Int, y: Int, level: EEC, cellwidth: Int, rotation: ROTATION, data: String) {
        ""
        val str =
            "QRCODE " + x + "," + y + "," + level.value + "," + cellwidth + "," + 'A' + "," + rotation.value + "," + "\"" + data + "\"" + "\r\n"
        this.addStrToCommand(str)
    }

    fun addQueryPrinterType() {
        ""
        val str = "~!T\r\n"
        this.addStrToCommand(str)
    }

    fun addQueryPrinterStatus() {
        command!!.add(27.toByte())
        command!!.add(33.toByte())
        command!!.add(63.toByte())
    }

    fun addResetPrinter() {
        command!!.add(27.toByte())
        command!!.add(33.toByte())
        command!!.add(82.toByte())
    }

    fun addQueryPrinterLife() {
        ""
        val str = "~!@\r\n"
        this.addStrToCommand(str)
    }

    fun addQueryPrinterMemory() {
        ""
        val str = "~!A\r\n"
        this.addStrToCommand(str)
    }

    fun addQueryPrinterFile() {
        ""
        val str = "~!F\r\n"
        this.addStrToCommand(str)
    }

    fun addQueryPrinterCodePage() {
        ""
        val str = "~!I\r\n"
        this.addStrToCommand(str)
    }

    fun addPeel(enable: ENABLE) {
        var str = ""
        if (enable.getValue().toInt() == 0) {
            str = "SET PEEL " + enable.getValue() + "\r\n"
        }

        this.addStrToCommand(str)
    }

    fun addTear(enable: ENABLE) {
        ""
        val str = "SET TEAR " + enable.getValue() + "\r\n"
        this.addStrToCommand(str)
    }

    fun addCutter(enable: ENABLE) {
        ""
        val str = "SET CUTTER " + enable.getValue() + "\r\n"
        this.addStrToCommand(str)
    }

    fun addCutterBatch() {
        val str = "SET CUTTER BATCH\r\n"
        this.addStrToCommand(str)
    }

    fun addCutterPieces(number: Short) {
        val str = "SET CUTTER $number\r\n"
        this.addStrToCommand(str)
    }

    fun addReprint(enable: ENABLE) {
        ""
        val str = "SET REPRINT " + enable.getValue() + "\r\n"
        this.addStrToCommand(str)
    }

    fun addPrintKey(enable: ENABLE) {
        ""
        val str = "SET PRINTKEY " + enable.getValue() + "\r\n"
        this.addStrToCommand(str)
    }

    fun addPrintKey(m: Int) {
        ""
        val str = "SET PRINTKEY $m\r\n"
        this.addStrToCommand(str)
    }

    fun addPartialCutter(enable: ENABLE) {
        ""
        val str = "SET PARTIAL_CUTTER " + enable.getValue() + "\r\n"
        this.addStrToCommand(str)
    }

    fun addUserCommand(command: String) {
        this.addStrToCommand(command)
    }

    enum class BARCODETYPE(val value: String) {
        CODE128("128"),
        CODE128M("128M"),
        EAN128("EAN128"),
        ITF25("25"),
        ITF25C("25C"),
        CODE39("39"),
        CODE39C("39C"),
        CODE39S("39S"),
        CODE93("93"),
        EAN13("EAN13"),
        EAN13_2("EAN13+2"),
        EAN13_5("EAN13+5"),
        EAN8("EAN8"),
        EAN8_2("EAN8+2"),
        EAN8_5("EAN8+5"),
        CODABAR("CODA"),
        POST("POST"),
        UPCA("UPCA"),
        UPCA_2("UPCA+2"),
        UPCA_5("UPCA+5"),
        UPCE("UPCE13"),
        UPCE_2("UPCE13+2"),
        UPCE_5("UPCE13+5"),
        CPOST("CPOST"),
        MSI("MSI"),
        MSIC("MSIC"),
        PLESSEY("PLESSEY"),
        ITF14("ITF14"),
        EAN14("EAN14")
    }

    enum class BITMAP_MODE(val value: Int) {
        OVERWRITE(0),
        OR(1),
        XOR(2)
    }

    enum class CODEPAGE(val value: Int) {
        PC437(437),
        PC850(850),
        PC852(852),
        PC860(860),
        PC863(863),
        PC865(865),
        WPC1250(1250),
        WPC1252(1252),
        WPC1253(1253),
        WPC1254(1254)
    }

    enum class DENSITY(val value: Int) {
        DNESITY0(0),
        DNESITY1(1),
        DNESITY2(2),
        DNESITY3(3),
        DNESITY4(4),
        DNESITY5(5),
        DNESITY6(6),
        DNESITY7(7),
        DNESITY8(8),
        DNESITY9(9),
        DNESITY10(10),
        DNESITY11(11),
        DNESITY12(12),
        DNESITY13(13),
        DNESITY14(14),
        DNESITY15(15)
    }

    enum class DIRECTION(val value: Int) {
        FORWARD(0),
        BACKWARD(1)
    }

    enum class EEC(val value: String) {
        LEVEL_L("L"),
        LEVEL_M("M"),
        LEVEL_Q("Q"),
        LEVEL_H("H")
    }

    enum class FONTMUL(val value: Int) {
        MUL_1(1),
        MUL_2(2),
        MUL_3(3),
        MUL_4(4),
        MUL_5(5),
        MUL_6(6),
        MUL_7(7),
        MUL_8(8),
        MUL_9(9),
        MUL_10(10)
    }

    enum class FONTTYPE(val value: String) {
        FONT_1("1"),
        FONT_2("2"),
        FONT_3("3"),
        FONT_4("4"),
        FONT_5("5"),
        FONT_6("6"),
        FONT_7("7"),
        FONT_8("8"),
        SIMPLIFIED_CHINESE("TSS24.BF2"),
        TRADITIONAL_CHINESE("TST24.BF2"),
        KOREAN("K")
    }

    enum class FOOT(val value: Int) {
        F2(0),
        F5(1)
    }

    enum class MIRROR(val value: Int) {
        NORMAL(0),
        MIRROR(1)
    }

    enum class READABEL(val value: Int) {
        DISABLE(0),
        EANBEL(1)
    }

    enum class ROTATION(val value: Int) {
        ROTATION_0(0),
        ROTATION_90(90),
        ROTATION_180(180),
        ROTATION_270(270)
    }

    enum class SPEED(val value: Float) {
        SPEED1DIV5(1.5f),
        SPEED2(2.0f),
        SPEED3(3.0f),
        SPEED4(4.0f)
    }

    companion object {
        private const val DEBUG_TAG = "LabelCommand"
    }
}