package com.ashish.csv_parser_android.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceReport(
    val serverID: String?,
    val deviceLines: List<DeviceLine>,
    val recordCount: Int?
)

@Serializable
data class DeviceLine(
    val IMEI1: String,
    val IMEI2: String,
    val serialNumber: String,
    val deviceName: String
)

@Serializable
data class EvoluteDeviceManagement(
    val deviceDetails: List<DeviceReport>
)

sealed class ParseState {
    object Idle : ParseState()
    object Loading : ParseState()
    data class Success(
        val deviceReport: DeviceReport,
        val outputString: String,
        val outputFormat: OutputFormat
    ) : ParseState()
    data class Error(val message: String) : ParseState()
}

enum class OutputFormat {
    JSON, XML
}

sealed class CsvLine {
    data class Header(val serverID: String) : CsvLine()
    data class Record(
        val IMEI1: String,
        val IMEI2: String,
        val serialNumber: String,
        val deviceName: String
    ) : CsvLine()
    data class Trailer(val count: Int) : CsvLine()
}

class CsvParseException(message: String) : Exception(message)
