package com.example.bluetooth_print;

import java.util.Vector


fun String.equallySplit(size: Int): List<String> {
    val ret = ArrayList<String>((length + size - 1) / size)

    var start = 0
    while (start < length) {
        ret.add(substring(start, Math.min(length, start + size)))
        start += size
    }
    return ret
}

fun String.formatStringForPrint(
        max: Int
) : String {
    var str = this
    var diff = max - str.length

    if (diff > 0) {
        val prefixString = StringBuilder()
        while (diff-- > 0) {
            prefixString.append(' ')
        }
        str = prefixString.toString().plus(str)
    }
    return str
}

fun Array<ByteArray>.toBytes(): ByteArray {
    var length = 0
    for (i in indices)
        length += this[i].size
    val send = ByteArray(length)
    var k = 0
    for (i in indices)
        for (j in 0 until this[i].size)
            send[k++] = this[i][j]
    return send
}

fun byteArrayToVector(byteArray: ByteArray): Vector<Byte> {
    val byteVector: Vector<Byte> = Vector(byteArray.size)
    for (b in byteArray) {
        byteVector.add(b)
    }
    return byteVector
}
