package com.ashish.csv_parser_android.parser

import com.ashish.csv_parser_android.model.CsvLine
import com.ashish.csv_parser_android.model.CsvParseException
import com.ashish.csv_parser_android.model.DeviceLine
import com.ashish.csv_parser_android.model.DeviceReport

class CsvParser {
    fun parse(content: String): DeviceReport {
        var serverID: String? = null
        val deviceLines = mutableListOf<DeviceLine>()
        var recordCount: Int? = null

        content.lineSequence()
            .filter { it.isNotBlank() }
            .forEach { line ->
                when (val parsedLine = parseLine(line)) {
                    is CsvLine.Header -> {
                        if (serverID != null) {
                            throw CsvParseException("Multiple header lines found")
                        }
                        serverID = parsedLine.serverID
                    }
                    is CsvLine.Record -> {
                        deviceLines.add(DeviceLine(
                            IMEI1 = parsedLine.IMEI1,
                            IMEI2 = parsedLine.IMEI2,
                            serialNumber = parsedLine.serialNumber,
                            deviceName = parsedLine.deviceName
                        ))
                    }
                    is CsvLine.Trailer -> {
                        if (recordCount != null) {
                            throw CsvParseException("Multiple trailer lines found")
                        }
                        recordCount = parsedLine.count
                    }
                }
            }

        // Initial mandatory check and compulsory params
        checkNotNull(serverID) { "No header record found" }
        checkNotNull(recordCount) { "No trailer record found" }

        // Secondary record count validate
        if (deviceLines.size != recordCount) {
            throw CsvParseException("Record count mismatch: expected $recordCount, found ${deviceLines.size}")
        }

        return DeviceReport(
            serverID = serverID,
            deviceLines = deviceLines,
            recordCount = recordCount
        )
    }

    private fun parseLine(line: String): CsvLine {
        val parts = line.split('|')

        return when (parts.getOrNull(0)) {
            "H" -> parseHeaderLine(parts)
            "R" -> parseRecordLine(parts)
            "T" -> parseTrailerLine(parts)
            else -> throw CsvParseException("Unknown record type: ${parts.getOrNull(0)}")
        }
    }

    private fun parseHeaderLine(parts: List<String>): CsvLine.Header {
        if (parts.size != 2) {
            throw CsvParseException("Malformed Header line: expected 2 parts, found ${parts.size}")
        }
        return CsvLine.Header(serverID = parts[1])
    }

    private fun parseRecordLine(parts: List<String>): CsvLine.Record {
        if (parts.size != 5) {
            throw CsvParseException("Malformed Record line: expected 5 parts, found ${parts.size}")
        }
        return CsvLine.Record(
            IMEI1 = parts[1],
            IMEI2 = parts[2],
            serialNumber = parts[3],
            deviceName = parts[4]
        )
    }

    private fun parseTrailerLine(parts: List<String>): CsvLine.Trailer {
        if (parts.size != 2) {
            throw CsvParseException("Malformed Trailer line: expected 2 parts, found ${parts.size}")
        }
        return try {
            CsvLine.Trailer(count = parts[1].toInt())
        } catch (e: NumberFormatException) {
            throw CsvParseException("Invalid count in Trailer line: ${parts[1]} is not a valid number")
        }
    }
}
