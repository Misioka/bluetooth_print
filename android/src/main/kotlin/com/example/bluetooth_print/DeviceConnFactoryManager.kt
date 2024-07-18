package com.example.bluetooth_print

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.gprinter.io.*
import java.io.IOException
import java.util.Objects
import java.util.Vector
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author thon
 */
class DeviceConnFactoryManager private constructor(build: Build) {
    var mPort: PortManager? = null

    /**
     * 获取端口连接方式
     */
    var connMethod: CONN_METHOD?

    /**
     * 获取连接蓝牙的物理地址
     */
    val macAddress: String?

    private val mContext: Context?

    /**
     * 获取端口打开状态（true 打开，false 未打开）
     */
    var connState: Boolean = false
        private set

    /**
     * ESC查询打印机实时状态指令
     */
    private val esc = byteArrayOf(0x10, 0x04, 0x02)

    /**
     * TSC查询打印机状态指令
     */
    private val tsc = byteArrayOf(0x1b, '!'.code.toByte(), '?'.code.toByte())

    private val cpcl = byteArrayOf(0x1b, 0x68)

    private var sendCommand: ByteArray

    /**
     * 判断打印机所使用指令是否是ESC指令
     */
    private var currentPrinterCommand: PrinterCommand? = null
    var reader: PrinterReader? = null
    private var queryPrinterCommandFlag = 0
    private val ESC = 1
    private val TSC = 3
    private val CPCL = 2

    enum class CONN_METHOD(override val name: String) {
        //蓝牙连接
        BLUETOOTH("BLUETOOTH"),

        //USB连接
        USB("USB"),

        //wifi连接
        WIFI("WIFI"),

        //串口连接
        SERIAL_PORT("SERIAL_PORT");

        override fun toString(): String {
            return this.name
        }
    }

    /**
     * 打开端口
     */
    fun openPort() {
        val deviceConnFactoryManager = deviceConnFactoryManagers[macAddress]
            ?: return

        deviceConnFactoryManager.connState = false
        if (deviceConnFactoryManager.connMethod == CONN_METHOD.BLUETOOTH) {
            mPort = BluetoothPort(macAddress)
            connState = deviceConnFactoryManager.mPort.openPort()
        }

        //端口打开成功后，检查连接打印机所使用的打印机指令ESC、TSC
        if (connState) {
            queryCommand()
        } else {
            if (this.mPort != null) {
                this.mPort = null
            }
        }
    }

    /**
     * 查询当前连接打印机所使用打印机指令（ESC（EscCommand.java）、TSC（LabelCommand.java））
     */
    private fun queryCommand() {
        //开启读取打印机返回数据线程
        reader = PrinterReader()
        reader!!.start() //读取数据线程
        //查询打印机所使用指令
        queryPrinterCommand() //小票机连接不上  注释这行，添加下面那三行代码。使用ESC指令
    }

    /**
     * 关闭端口
     */
    fun closePort() {
        if (this.mPort != null) {
            if (reader != null) {
                reader!!.cancel()
                reader = null
            }
            val b: Boolean = mPort.closePort()
            if (b) {
                this.mPort = null
                connState = false
                currentPrinterCommand = null
            }

            Log.e(
                TAG,
                "******************* close Port macAddress -> $macAddress"
            )
        }
    }

    /**
     * 获取当前打印机指令
     *
     * @return PrinterCommand
     */
    fun getCurrentPrinterCommand(): PrinterCommand? {
        return Objects.requireNonNull(deviceConnFactoryManagers[macAddress])!!.currentPrinterCommand
    }

    class Build {
        var macAddress: String? = null
        var connMethod: CONN_METHOD? = null
        var context: Context? = null

        fun setMacAddress(macAddress: String?): Build {
            this.macAddress = macAddress
            return this
        }

        fun setConnMethod(connMethod: CONN_METHOD?): Build {
            this.connMethod = connMethod
            return this
        }

        fun setContext(context: Context?): Build {
            this.context = context
            return this
        }

        fun build(): DeviceConnFactoryManager {
            return DeviceConnFactoryManager(this)
        }
    }

    fun sendDataImmediately(data: Vector<Byte>) {
        if (this.mPort == null) {
            return
        }
        try {
            mPort.writeDataImmediately(data, 0, data.size)
        } catch (e: Exception) { //异常中断发送
            mHandler.obtainMessage(Constant.abnormal_Disconnection).sendToTarget()

            //            e.printStackTrace();
        }
    }

    fun sendByteDataImmediately(data: ByteArray) {
        if (this.mPort != null) {
            val datas = Vector<Byte>()
            for (datum in data) {
                datas.add(datum)
            }
            try {
                mPort.writeDataImmediately(datas, 0, datas.size)
            } catch (e: IOException) { //异常中断
                mHandler.obtainMessage(Constant.abnormal_Disconnection).sendToTarget()
            }
        }
    }

    fun readDataImmediately(buffer: ByteArray?): Int {
        var r = 0
        if (this.mPort == null) {
            return r
        }

        try {
            r = mPort.readData(buffer)
        } catch (e: IOException) {
            closePort()
        }

        return r
    }

    /**
     * 查询打印机当前使用的指令（ESC、CPCL、TSC、）
     */
    private fun queryPrinterCommand() {
        queryPrinterCommandFlag = ESC
        com.example.bluetooth_print.ThreadPool.getInstantiation().addSerialTask(Runnable {
            //开启计时器，隔2000毫秒没有没返回值时发送查询打印机状态指令，先发票据，面单，标签
            val threadFactoryBuilder: ThreadFactoryBuilder = ThreadFactoryBuilder("Timer")
            val scheduledExecutorService: ScheduledExecutorService =
                ScheduledThreadPoolExecutor(1, threadFactoryBuilder)
            scheduledExecutorService.scheduleAtFixedRate(threadFactoryBuilder.newThread(object :
                Runnable {
                override fun run() {
                    if (currentPrinterCommand == null && queryPrinterCommandFlag > TSC) {
                        if (reader != null) { //三种状态，查询无返回值，发送连接失败广播
                            reader!!.cancel()
                            mPort.closePort()
                            this.connState = false

                            scheduledExecutorService.shutdown()
                        }
                    }
                    if (currentPrinterCommand != null) {
                        if (!scheduledExecutorService.isShutdown) {
                            scheduledExecutorService.shutdown()
                        }
                        return
                    }
                    when (queryPrinterCommandFlag) {
                        ESC ->                                 //发送ESC查询打印机状态指令
                            sendCommand = esc

                        TSC ->                                 //发送ESC查询打印机状态指令
                            sendCommand = tsc

                        CPCL ->                                 //发送CPCL查询打印机状态指令
                            sendCommand = cpcl

                        else -> {}
                    }
                    val data = Vector<Byte>(sendCommand.size)
                    for (b in sendCommand) {
                        data.add(b)
                    }
                    sendDataImmediately(data)
                    queryPrinterCommandFlag++
                }
            }), 1500, 1500, TimeUnit.MILLISECONDS)
        })
    }

    inner class PrinterReader : Thread() {
        private var isRun = false
        private val buffer = ByteArray(100)

        init {
            isRun = true
        }

        override fun run() {
            try {
                while (isRun && mPort != null) {
                    //读取打印机返回信息,打印机没有返回纸返回-1
                    Log.e(TAG, "******************* wait read ")
                    val len = readDataImmediately(buffer)
                    Log.e(
                        TAG,
                        "******************* read $len"
                    )
                    if (len > 0) {
                        val message: Message = Message.obtain()
                        message.what = READ_DATA
                        val bundle: Bundle = Bundle()
                        bundle.putInt(READ_DATA_CNT, len) //数据长度
                        bundle.putByteArray(READ_BUFFER_ARRAY, buffer) //数据
                        message.setData(bundle)
                        mHandler.sendMessage(message)
                    }
                }
            } catch (e: Exception) { //异常断开
                if (deviceConnFactoryManagers[macAddress] != null) {
                    closePort()
                    mHandler.obtainMessage(Constant.abnormal_Disconnection).sendToTarget()
                }
            }
        }

        fun cancel() {
            isRun = false
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        fun handleMessage(msg: Message) {
            when (msg.what) {
                Constant.abnormal_Disconnection -> {
                    Log.d(TAG, "******************* abnormal disconnection")
                    sendStateBroadcast(Constant.abnormal_Disconnection)
                }

                DEFAUIT_COMMAND -> {}
                READ_DATA -> {
                    val cnt: Int = msg.getData().getInt(READ_DATA_CNT) //数据长度 >0;
                    val buffer: ByteArray = msg.getData().getByteArray(READ_BUFFER_ARRAY)
                        ?: return //数据
                    //这里只对查询状态返回值做处理，其它返回值可参考编程手册来解析
                    val result = judgeResponseType(buffer[0]) //数据右移
                    var status = ""
                    if (sendCommand == esc) {
                        //设置当前打印机模式为ESC模式
                        if (currentPrinterCommand == null) {
                            currentPrinterCommand = PrinterCommand.ESC
                            sendStateBroadcast(CONN_STATE_CONNECTED)
                        } else { //查询打印机状态
                            if (result == 0) { //打印机状态查询
                                val intent: Intent = Intent(ACTION_QUERY_PRINTER_STATE)
                                intent.putExtra(DEVICE_ID, macAddress)
                                if (mContext != null) {
                                    mContext.sendBroadcast(intent)
                                }
                            } else if (result == 1) { //查询打印机实时状态
                                if ((buffer[0].toInt() and ESC_STATE_PAPER_ERR) > 0) {
                                    status += "*******************  Printer out of paper"
                                }
                                if ((buffer[0].toInt() and ESC_STATE_COVER_OPEN) > 0) {
                                    status += "*******************  Printer open cover"
                                }
                                if ((buffer[0].toInt() and ESC_STATE_ERR_OCCURS) > 0) {
                                    status += "*******************  Printer error"
                                }
                                Log.d(TAG, status)
                            }
                        }
                    } else if (sendCommand == tsc) {
                        //设置当前打印机模式为TSC模式
                        if (currentPrinterCommand == null) {
                            currentPrinterCommand = PrinterCommand.TSC
                            sendStateBroadcast(CONN_STATE_CONNECTED)
                        } else {
                            if (cnt == 1) { //查询打印机实时状态
                                if ((buffer[0].toInt() and TSC_STATE_PAPER_ERR) > 0) {
                                    //缺纸
                                    status += "*******************  Printer out of paper"
                                }
                                if ((buffer[0].toInt() and TSC_STATE_COVER_OPEN) > 0) {
                                    //开盖
                                    status += "*******************  Printer open cover"
                                }
                                if ((buffer[0].toInt() and TSC_STATE_ERR_OCCURS) > 0) {
                                    //打印机报错
                                    status += "*******************  Printer error"
                                }
                                Log.d(TAG, status)
                            } else { //打印机状态查询
                                val intent: Intent = Intent(ACTION_QUERY_PRINTER_STATE)
                                intent.putExtra(DEVICE_ID, macAddress)
                                if (mContext != null) {
                                    mContext.sendBroadcast(intent)
                                }
                            }
                        }
                    } else if (sendCommand == cpcl) {
                        if (currentPrinterCommand == null) {
                            currentPrinterCommand = PrinterCommand.CPCL
                            sendStateBroadcast(CONN_STATE_CONNECTED)
                        } else {
                            if (cnt == 1) {
                                if ((buffer[0].toInt() == CPCL_STATE_PAPER_ERR)) { //缺纸
                                    status += "*******************  Printer out of paper"
                                }
                                if ((buffer[0].toInt() == CPCL_STATE_COVER_OPEN)) { //开盖
                                    status += "*******************  Printer open cover"
                                }
                                Log.d(TAG, status)
                            } else { //打印机状态查询
                                val intent: Intent = Intent(ACTION_QUERY_PRINTER_STATE)
                                intent.putExtra(DEVICE_ID, macAddress)
                                if (mContext != null) {
                                    mContext.sendBroadcast(intent)
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    init {
        this.connMethod = build.connMethod
        this.macAddress = build.macAddress
        this.mContext = build.context
        deviceConnFactoryManagers[build.macAddress] =
            this
    }

    /**
     * 发送广播
     */
    private fun sendStateBroadcast(state: Int) {
        val intent: Intent = Intent(ACTION_CONN_STATE)
        intent.putExtra(STATE, state)
        intent.putExtra(DEVICE_ID, macAddress)
        if (mContext != null) {
            mContext.sendBroadcast(intent) //此处若报空指针错误，需要在清单文件application标签里注册此类，参考demo
        }
    }

    /**
     * 判断是实时状态（10 04 02）还是查询状态（1D 72 01）
     */
    private fun judgeResponseType(r: Byte): Int {
        return ((r.toInt() and FLAG.toInt()) shr 4).toByte()
            .toInt()
    }

    companion object {
        private val TAG: String = DeviceConnFactoryManager::class.java.simpleName

        private val deviceConnFactoryManagers: MutableMap<String?, DeviceConnFactoryManager?> =
            HashMap()

        /**
         * ESC查询打印机实时状态 缺纸状态
         */
        private const val ESC_STATE_PAPER_ERR = 0x20

        /**
         * ESC指令查询打印机实时状态 打印机开盖状态
         */
        private const val ESC_STATE_COVER_OPEN = 0x04

        /**
         * ESC指令查询打印机实时状态 打印机报错状态
         */
        private const val ESC_STATE_ERR_OCCURS = 0x40

        /**
         * TSC指令查询打印机实时状态 打印机缺纸状态
         */
        private const val TSC_STATE_PAPER_ERR = 0x04

        /**
         * TSC指令查询打印机实时状态 打印机开盖状态
         */
        private const val TSC_STATE_COVER_OPEN = 0x01

        /**
         * TSC指令查询打印机实时状态 打印机出错状态
         */
        private const val TSC_STATE_ERR_OCCURS = 0x80

        /**
         * CPCL指令查询打印机实时状态 打印机缺纸状态
         */
        private const val CPCL_STATE_PAPER_ERR = 0x01

        /**
         * CPCL指令查询打印机实时状态 打印机开盖状态
         */
        private const val CPCL_STATE_COVER_OPEN = 0x02

        const val FLAG: Byte = 0x10
        private const val READ_DATA = 10000
        private const val DEFAUIT_COMMAND = 20000
        private const val READ_DATA_CNT = "read_data_cnt"
        private const val READ_BUFFER_ARRAY = "read_buffer_array"
        const val ACTION_CONN_STATE: String = "action_connect_state"
        const val ACTION_QUERY_PRINTER_STATE: String = "action_query_printer_state"
        const val STATE: String = "state"
        const val DEVICE_ID: String = "id"
        const val CONN_STATE_DISCONNECT: Int = 0x90
        const val CONN_STATE_CONNECTED: Int = CONN_STATE_DISCONNECT shl 3
        fun getDeviceConnFactoryManagers(): Map<String?, DeviceConnFactoryManager?> {
            return deviceConnFactoryManagers
        }

        fun closeAllPort() {
            for (deviceConnFactoryManager in deviceConnFactoryManagers.values) {
                if (deviceConnFactoryManager != null) {
                    Log.e(
                        TAG,
                        "******************* close All Port macAddress -> " + deviceConnFactoryManager.macAddress
                    )

                    deviceConnFactoryManager.closePort()
                    deviceConnFactoryManagers[deviceConnFactoryManager.macAddress] =
                        null
                }
            }
        }
    }
}