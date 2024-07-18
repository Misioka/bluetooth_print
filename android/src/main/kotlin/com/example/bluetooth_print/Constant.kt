package com.example.bluetooth_print

object Constant {
    const val SERIALPORTPATH: String = "SerialPortPath"
    const val SERIALPORTBAUDRATE: String = "SerialPortBaudrate"
    const val WIFI_CONFIG_IP: String = "wifi config ip"
    const val WIFI_CONFIG_PORT: String = "wifi config port"
    const val ACTION_USB_PERMISSION: String = "com.android.example.USB_PERMISSION"
    const val BLUETOOTH_REQUEST_CODE: Int = 0x001
    const val USB_REQUEST_CODE: Int = 0x002
    const val WIFI_REQUEST_CODE: Int = 0x003
    const val SERIALPORT_REQUEST_CODE: Int = 0x006
    const val CONN_STATE_DISCONN: Int = 0x007
    const val MESSAGE_UPDATE_PARAMETER: Int = 0x009
    const val tip: Int = 0x010
    const val abnormal_Disconnection: Int = 0x011 //异常断开

    /**
     * wifi 默认ip
     */
    const val WIFI_DEFAULT_IP: String = "192.168.123.100"

    /**
     * wifi 默认端口号
     */
    const val WIFI_DEFAULT_PORT: Int = 9100
}