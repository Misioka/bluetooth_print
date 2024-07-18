package com.example.bluetooth_print.print;

enum class PrinterType(val charset: String, val ratios: HashMap<String, IntArray>, vararg val printerNames: String) {
    ZEBRA("cp1250",
            hashMapOf(PrinterType.VAT_RATIO_KEY to intArrayOf(5, 5, 3, 3),
                    PrinterType.DEALS_HEADLINE_RATIO_KEY to intArrayOf(10, 9, 6, 13, 2),
                    PrinterType.DEALS_VALUE_RATIO_KEY to intArrayOf(13, 7, 5, 7, 9),
                    PrinterType.TOTAL_RATIO_KEY to intArrayOf(6, 1, 5),
                    PrinterType.SUMMARY_PRODUCT_RATIO_KEY to intArrayOf(4, 8, 2, 4),
                    PrinterType.SUMMARY_MONEY_RATIO_KEY to intArrayOf(7, 12, 6),
                    PrinterType.SUPPLY_TIME_RATIO_KEY to intArrayOf(2, 1),
                    PrinterType.PACKAGE_RATIO_KEY to intArrayOf(6, 11, 5),
                    PrinterType.PACKAGE2_RATIO_KEY to intArrayOf(6, 9, 5),
                    PrinterType.SUMMARY_ROUTE_RATIO_KEY to intArrayOf(1, 3),
                    PrinterType.VAT_VALUE_RATIO_KEY to intArrayOf(1, 1, 1, 1),
                    PrinterType.SIGNATURE_LABELS_RATIO_KEY to intArrayOf(1, 1)),
            "MZ320", "iMZ320", "ZQ320"),

    PTP("UTF-8",
            hashMapOf(PrinterType.VAT_RATIO_KEY to intArrayOf(12, 12, 14, 10),
                    PrinterType.DEALS_HEADLINE_RATIO_KEY to intArrayOf(14, 9, 6, 13, 6),
                    PrinterType.DEALS_VALUE_RATIO_KEY to intArrayOf(14, 10, 6, 9, 9),
                    PrinterType.TOTAL_RATIO_KEY to intArrayOf(24, 8, 16),
                    PrinterType.SUMMARY_PRODUCT_RATIO_KEY to intArrayOf(12, 18, 7, 10),
                    PrinterType.SUMMARY_MONEY_RATIO_KEY to intArrayOf(14, 23, 11),
                    PrinterType.SUPPLY_TIME_RATIO_KEY to intArrayOf(32, 16),
                    PrinterType.PACKAGE_RATIO_KEY to intArrayOf(36, 1, 11),
                    PrinterType.PACKAGE2_RATIO_KEY to intArrayOf(36, 1, 11),
                    PrinterType.SUMMARY_ROUTE_RATIO_KEY to intArrayOf(12, 36),
                    PrinterType.VAT_VALUE_RATIO_KEY to intArrayOf(12, 12, 14, 10),
                    PrinterType.SIGNATURE_LABELS_RATIO_KEY to intArrayOf(24, 24)),
            "PTP");

    companion object {
        const val VAT_RATIO_KEY = "vatRatio"
        const val VAT_VALUE_RATIO_KEY = "vatValueRatio"
        const val DEALS_HEADLINE_RATIO_KEY = "dealsHeadlineRatio"
        const val DEALS_VALUE_RATIO_KEY = "dealsValueRatio"
        const val TOTAL_RATIO_KEY = "totalRatio"
        const val SUMMARY_PRODUCT_RATIO_KEY = "summaryProductRatio"
        const val SUMMARY_MONEY_RATIO_KEY = "summaryMoneyRatio"
        const val SUPPLY_TIME_RATIO_KEY = "supplyTimeRatio"
        const val PACKAGE_RATIO_KEY = "packageRatio"
        const val PACKAGE2_RATIO_KEY = "package2Ratio"
        const val SUMMARY_ROUTE_RATIO_KEY = "summaryRouteRatio"
        const val SIGNATURE_LABELS_RATIO_KEY = "signatureLabelsRatio"

        private val smallPrinters: List<String> = arrayListOf("PTP-II")

        fun getPrinterTypeByName(name: String?): PrinterType? {
            values().forEach {
                for (printerName in it.printerNames) {
                    if (name?.contains(printerName) == true)
                        return it
                }
            }

            return null
        }

        fun isSmallPrinter(name: String?): Boolean {
            smallPrinters.forEach {
                if (it == name) return true
            }

            return false
        }
    }
}