package com.example.bluetooth_print

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gprinter.command.FactoryCommand
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener

import java.util.ArrayList
import java.util.HashMap

/**
 * BluetoothPrintPlugin
 * @author thon
 */
class BluetoothPrintPlugin : FlutterPlugin, ActivityAware, MethodCallHandler,
    RequestPermissionsResultListener {
    private val initializationLock = Any()
    private var context: Context? = null
    private var threadPool: ThreadPool? = null
    private var curMacAddress: String? = null

    private var channel: MethodChannel? = null
    private var stateChannel: EventChannel? = null
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var pluginBinding: FlutterPluginBinding? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var application: Application? = null
    private var activity: Activity? = null

    private var pendingCall: MethodCall? = null
    private var pendingResult: Result? = null
    fun onAttachedToEngine(binding: FlutterPluginBinding?) {
        pluginBinding = binding
    }

    fun onDetachedFromEngine(binding: FlutterPluginBinding?) {
        pluginBinding = null
    }

    fun onAttachedToActivity(binding: ActivityPluginBinding?) {
        activityBinding = binding
        setup(
            pluginBinding.getBinaryMessenger(),
            pluginBinding.getApplicationContext() as Application,
            activityBinding.getActivity(),
            null,
            activityBinding
        )
    }

    fun onDetachedFromActivity() {
        tearDown()
    }

    fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding?) {
        onAttachedToActivity(binding)
    }

    private fun setup(
        messenger: BinaryMessenger,
        application: Application,
        activity: Activity,
        registrar: PluginRegistry.Registrar?,
        activityBinding: ActivityPluginBinding?
    ) {
        synchronized(initializationLock) {
            Log.i(com.example.bluetooth_print.BluetoothPrintPlugin.Companion.TAG, "setup")
            this.activity = activity
            this.application = application
            this.context = application
            channel = MethodChannel(
                messenger,
                com.example.bluetooth_print.BluetoothPrintPlugin.Companion.NAMESPACE + "/methods"
            )
            channel.setMethodCallHandler(this)
            stateChannel = EventChannel(
                messenger,
                com.example.bluetooth_print.BluetoothPrintPlugin.Companion.NAMESPACE + "/state"
            )
            stateChannel.setStreamHandler(stateHandler)
            mBluetoothManager =
                application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothAdapter = mBluetoothManager.getAdapter()
            if (registrar != null) {
                // V1 embedding setup for activity listeners.
                registrar.addRequestPermissionsResultListener(this)
            } else {
                // V2 embedding setup for activity listeners.
                activityBinding.addRequestPermissionsResultListener(this)
            }
        }
    }

    private fun tearDown() {
        Log.i(com.example.bluetooth_print.BluetoothPrintPlugin.Companion.TAG, "teardown")
        context = null
        activityBinding.removeRequestPermissionsResultListener(this)
        activityBinding = null
        channel.setMethodCallHandler(null)
        channel = null
        stateChannel.setStreamHandler(null)
        stateChannel = null
        mBluetoothAdapter = null
        mBluetoothManager = null
        application = null
    }


    fun onMethodCall(call: MethodCall, result: Result) {
        if (mBluetoothAdapter == null && "isAvailable" != call.method) {
            result.error("bluetooth_unavailable", "the device does not have bluetooth", null)
            return
        }

        when (call.method) {
            "state" -> state(result)
            "isAvailable" -> result.success(mBluetoothAdapter != null)
            "isOn" -> result.success(mBluetoothAdapter.isEnabled())
            "isConnected" -> result.success(threadPool != null)
            "startScan" -> {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activityBinding.getActivity(),
                        com.example.bluetooth_print.BluetoothPrintPlugin.Companion.PERMISSIONS_LOCATION,
                        com.example.bluetooth_print.BluetoothPrintPlugin.Companion.REQUEST_FINE_LOCATION_PERMISSIONS
                    )
                    pendingCall = call
                    pendingResult = result
                    break
                }

                startScan(call, result)
            }

            "stopScan" -> {
                stopScan()
                result.success(null)
            }

            "connect" -> connect(call, result)
            "disconnect" -> result.success(disconnect())
            "destroy" -> result.success(destroy())
            "print", "printReceipt", "printLabel" -> print(call, result)
            "printTest" -> printTest(result)
            else -> result.notImplemented()
        }
    }

    private fun getDevices(result: Result) {
        val devices: MutableList<Map<String, Any>> = ArrayList()
        for (device in mBluetoothAdapter.getBondedDevices()) {
            val ret: MutableMap<String, Any> = HashMap()
            ret["address"] = device.getAddress()
            ret["name"] = device.getName()
            ret["type"] = device.getType()
            devices.add(ret)
        }

        result.success(devices)
    }

    /**
     * 获取状态
     */
    private fun state(result: Result) {
        try {
            when (mBluetoothAdapter.getState()) {
                BluetoothAdapter.STATE_OFF -> result.success(BluetoothAdapter.STATE_OFF)
                BluetoothAdapter.STATE_ON -> result.success(BluetoothAdapter.STATE_ON)
                BluetoothAdapter.STATE_TURNING_OFF -> result.success(BluetoothAdapter.STATE_TURNING_OFF)
                BluetoothAdapter.STATE_TURNING_ON -> result.success(BluetoothAdapter.STATE_TURNING_ON)
                else -> result.success(0)
            }
        } catch (e: SecurityException) {
            result.error("invalid_argument", "argument 'address' not found", null)
        }
    }


    private fun startScan(call: MethodCall, result: Result) {
        Log.d(com.example.bluetooth_print.BluetoothPrintPlugin.Companion.TAG, "start scan ")

        try {
            startScan()
            result.success(null)
        } catch (e: Exception) {
            result.error("startScan", e.message, e)
        }
    }

    private fun invokeMethodUIThread(name: String, device: BluetoothDevice) {
        val ret: MutableMap<String, Any> = java.util.HashMap()
        ret["address"] = device.getAddress()
        ret["name"] = device.getName()
        ret["type"] = device.getType()

        activity.runOnUiThread(
            Runnable { channel.invokeMethod(name, ret) })
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.getDevice()
            if (device != null && device.getName() != null) {
                invokeMethodUIThread("ScanResult", device)
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun startScan() {
        val scanner: BluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner()
            ?: throw IllegalStateException("getBluetoothLeScanner() is null. Is the Adapter on?")

        // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
        val settings: ScanSettings =
            Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(null, settings, mScanCallback)
    }

    private fun stopScan() {
        val scanner: BluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner()
        if (scanner != null) {
            scanner.stopScan(mScanCallback)
        }
    }

    /**
     * 连接
     */
    private fun connect(call: MethodCall, result: Result) {
        val args: Map<String, Any> = call.arguments()
        if (args != null && args.containsKey("address")) {
            val address = args["address"] as String?
            this.curMacAddress = address

            disconnect()

            Build() //设置连接方式
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH) //设置连接的蓝牙mac地址
                .setMacAddress(address)
                .build()

            //打开端口
            threadPool = ThreadPool.getInstantiation()
            threadPool.addSerialTask(Runnable {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(address).openPort()
            })

            result.success(true)
        } else {
            result.error(
                "******************* invalid_argument",
                "argument 'address' not found",
                null
            )
        }
    }

    /**
     * 关闭连接
     */
    private fun disconnect(): Boolean {
        val deviceConnFactoryManager: DeviceConnFactoryManager =
            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(curMacAddress)
        if (deviceConnFactoryManager != null && deviceConnFactoryManager.mPort != null) {
            deviceConnFactoryManager.reader.cancel()
            deviceConnFactoryManager.closePort()
            deviceConnFactoryManager.mPort = null
        }

        return true
    }

    private fun destroy(): Boolean {
        DeviceConnFactoryManager.closeAllPort()
        if (threadPool != null) {
            threadPool.stopThreadPool()
        }

        return true
    }

    private fun printTest(result: Result) {
        val deviceConnFactoryManager: DeviceConnFactoryManager =
            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(curMacAddress)
        if (deviceConnFactoryManager == null || !deviceConnFactoryManager.getConnState()) {
            result.error("not connect", "state not right", null)
        }

        threadPool = ThreadPool.getInstantiation()
        threadPool.addSerialTask(Runnable {
            checkNotNull(deviceConnFactoryManager)
            val printerCommand: PrinterCommand = deviceConnFactoryManager.getCurrentPrinterCommand()
            if (printerCommand === PrinterCommand.ESC) {
                deviceConnFactoryManager.sendByteDataImmediately(
                    FactoryCommand.printSelfTest(
                        FactoryCommand.printerMode.ESC
                    )
                )
            } else if (printerCommand === PrinterCommand.TSC) {
                deviceConnFactoryManager.sendByteDataImmediately(
                    FactoryCommand.printSelfTest(
                        FactoryCommand.printerMode.TSC
                    )
                )
            } else if (printerCommand === PrinterCommand.CPCL) {
                deviceConnFactoryManager.sendByteDataImmediately(
                    FactoryCommand.printSelfTest(
                        FactoryCommand.printerMode.CPCL
                    )
                )
            }
        })
    }

    private fun print(call: MethodCall, result: Result) {
        val args: Map<String, Any> = call.arguments()

        val deviceConnFactoryManager: DeviceConnFactoryManager =
            DeviceConnFactoryManager.getDeviceConnFactoryManagers().get(curMacAddress)
        if (deviceConnFactoryManager == null || !deviceConnFactoryManager.getConnState()) {
            result.error("not connect", "state not right", null)
        }

        if (args != null && args.containsKey("config") && args.containsKey("data")) {
            val config = args["config"] as Map<String, Any>?
            val list = args["data"] as List<Map<String, Any>>?
                ?: return

            threadPool = ThreadPool.getInstantiation()
            threadPool.addSerialTask(Runnable {
                checkNotNull(deviceConnFactoryManager)
                val printerCommand: PrinterCommand =
                    deviceConnFactoryManager.getCurrentPrinterCommand()
                if (printerCommand === PrinterCommand.ESC) {
                    deviceConnFactoryManager.sendDataImmediately(
                        PrintContent.mapToReceipt(
                            config,
                            list
                        )
                    )
                } else if (printerCommand === PrinterCommand.TSC) {
                    deviceConnFactoryManager.sendDataImmediately(
                        PrintContent.mapToLabel(
                            config,
                            list
                        )
                    )
                } else if (printerCommand === PrinterCommand.CPCL) {
                    deviceConnFactoryManager.sendDataImmediately(
                        PrintContent.mapToCPCL(
                            config,
                            list
                        )
                    )
                }
            })
        } else {
            result.error("please add config or data", "", null)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == com.example.bluetooth_print.BluetoothPrintPlugin.Companion.REQUEST_FINE_LOCATION_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan(pendingCall, pendingResult)
            } else {
                pendingResult.error(
                    "no_permissions",
                    "this plugin requires location permissions for scanning",
                    null
                )
                pendingResult = null
            }
            return true
        }
        return false
    }


    private val stateHandler: StreamHandler = object : StreamHandler() {
        private var sink: EventSink? = null

        private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            fun onReceive(context: Context?, intent: Intent) {
                val action: String = intent.getAction()
                Log.d(
                    com.example.bluetooth_print.BluetoothPrintPlugin.Companion.TAG,
                    "stateStreamHandler, current action: $action"
                )

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    threadPool = null
                    sink.success(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1))
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    sink.success(1)
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    threadPool = null
                    sink.success(0)
                }
            }
        }

        fun onListen(o: Any?, eventSink: EventSink?) {
            sink = eventSink
            val filter: IntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            context.registerReceiver(mReceiver, filter)
        }

        fun onCancel(o: Any?) {
            sink = null
            context.unregisterReceiver(mReceiver)
        }
    }

    companion object {
        private const val TAG = "BluetoothPrintPlugin"
        private const val NAMESPACE = "bluetooth_print"
        private const val REQUEST_FINE_LOCATION_PERMISSIONS = 1452

        private val PERMISSIONS_LOCATION = arrayOf<String>(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        fun registerWith(registrar: Registrar) {
            val instance: com.example.bluetooth_print.BluetoothPrintPlugin =
                com.example.bluetooth_print.BluetoothPrintPlugin()

            val activity: Activity = registrar.activity()
            var application: Application? = null
            if (registrar.context() != null) {
                application = registrar.context().getApplicationContext() as Application
            }
            instance.setup(registrar.messenger(), application, activity, registrar, null)
        }
    }
}