package android.src.main.java.com.example.bluetooth_print

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import java.util.Locale

/**
 * Created by imrankst1221@gmail.com
 *
 */
object PtpBitmapUtils {
    private const val hexStr = "0123456789ABCDEF"
    private val binaryArray = arrayOf(
        "0000", "0001", "0010", "0011",
        "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
        "1100", "1101", "1110", "1111"
    )

    fun decodeTwoBitmaps(
        _bmp1: Bitmap?,
        _bmp2: Bitmap?,
        width: Int,
        height: Int,
        resultWidth: Int,
        resultHeight: Int
    ): ByteArray? {
        val bmp1: Bitmap = Bitmap.createScaledBitmap(_bmp1, width, height, false)
        val bmp2: Bitmap = Bitmap.createScaledBitmap(_bmp2, width, height, false)

        val result: Bitmap = Bitmap.createBitmap(resultWidth, resultHeight, bmp1.getConfig())
        val canvas: Canvas = Canvas(result)
        canvas.drawBitmap(bmp1, 0f, 0f, null)
        canvas.drawBitmap(bmp2, width, 0f, null)
        return decodeBitmap(result, resultWidth, resultHeight)
    }

    fun decodeBitmap(_bmp: Bitmap?, width: Int, height: Int): ByteArray? {
        val bmp: Bitmap = Bitmap.createScaledBitmap(_bmp, width, height, false)

        val bmpWidth: Int = bmp.getWidth()
        val bmpHeight: Int = bmp.getHeight()

        val list: MutableList<String> = ArrayList() //binaryString list
        var sb: StringBuffer


        var bitLen = bmpWidth / 8
        val zeroCount = bmpWidth % 8

        var zeroStr = ""
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1
            for (i in 0 until (8 - zeroCount)) {
                zeroStr = zeroStr + "0"
            }
        }

        for (i in 0 until bmpHeight) {
            sb = StringBuffer()
            for (j in 0 until bmpWidth) {
                val color: Int = bmp.getPixel(j, i)

                val r = (color shr 16) and 0xff
                val g = (color shr 8) and 0xff
                val b = color and 0xff

                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160) sb.append("0")
                else sb.append("1")
            }
            if (zeroCount > 0) {
                sb.append(zeroStr)
            }
            list.add(sb.toString())
        }

        val bmpHexList = binaryListToHexStringList(list)
        val commandHexString = "1D763000"
        var widthHexString = Integer
            .toHexString(
                if (bmpWidth % 8 == 0) bmpWidth / 8
                else (bmpWidth / 8 + 1)
            )
        if (widthHexString.length > 2) {
            Log.e("decodeBitmap error", " width is too large")
            return null
        } else if (widthHexString.length == 1) {
            widthHexString = "0$widthHexString"
        }
        widthHexString = widthHexString + "00"

        var heightHexString = Integer.toHexString(bmpHeight)
        if (heightHexString.length > 3) {
            Log.e("decodeBitmap error", " height is too large")
            return null
        } else if (heightHexString.length == 1) {
            heightHexString = "0$heightHexString"
        }
        heightHexString = heightHexString + "00"

        val commandList: MutableList<String> = ArrayList()
        commandList.add(commandHexString + widthHexString + heightHexString)
        commandList.addAll(bmpHexList)

        return hexList2Byte(commandList)
    }

    private fun binaryListToHexStringList(list: List<String>): List<String> {
        val hexList: MutableList<String> = ArrayList()
        for (binaryStr in list) {
            val sb = StringBuffer()
            var i = 0
            while (i < binaryStr.length) {
                val str = binaryStr.substring(i, i + 8)

                val hexString = myBinaryStrToHexString(str)
                sb.append(hexString)
                i += 8
            }
            hexList.add(sb.toString())
        }
        return hexList
    }

    private fun myBinaryStrToHexString(binaryStr: String): String {
        var hex = ""
        val f4 = binaryStr.substring(0, 4)
        val b4 = binaryStr.substring(4, 8)
        for (i in binaryArray.indices) {
            if (f4 == binaryArray[i]) hex += hexStr.substring(i, i + 1)
        }
        for (i in binaryArray.indices) {
            if (b4 == binaryArray[i]) hex += hexStr.substring(i, i + 1)
        }

        return hex
    }

    private fun hexList2Byte(list: List<String>): ByteArray {
        val commandList: MutableList<ByteArray?> = ArrayList()

        for (hexStr in list) {
            commandList.add(hexStringToBytes(hexStr))
        }
        val bytes = sysCopy(commandList)
        return bytes
    }

    private fun hexStringToBytes(hexString: String): ByteArray? {
        var hexString: String? = hexString
        if (hexString == null || hexString == "") {
            return null
        }
        hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            d[i] = (charToByte(
                hexChars[pos]
            ).toInt() shl 4 or charToByte(
                hexChars[pos + 1]
            ).toInt()).toByte()
        }
        return d
    }

    private fun sysCopy(srcArrays: List<ByteArray?>): ByteArray {
        var len = 0
        for (srcArray in srcArrays) {
            len += srcArray!!.size
        }
        val destArray = ByteArray(len)
        var destLen = 0
        for (srcArray in srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray!!.size)
            destLen += srcArray.size
        }
        return destArray
    }

    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }
}