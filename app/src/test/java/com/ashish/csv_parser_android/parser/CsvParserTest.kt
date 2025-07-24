package com.ashish.csv_parser_android.parser

import com.ashish.csv_parser_android.model.CsvParseException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CsvParserTest {
    private lateinit var csvParser: CsvParser

    @Before
    fun setup() {
        csvParser = CsvParser()
    }

    @Test
    fun `test parse valid CSV content`() {
        val csvContent = """
            RecordType,ServerID,IMEI1,IMEI2,SerialNumber,DeviceName,Count
            1,HDKF190320201903202020032020171931,123400000000000,123400000000060,A5123456700000250,UNIPOS A5,9
        """.trimIndent()

        val result = csvParser.parse(csvContent)

        assertNotNull(result)
        assertEquals("HDKF190320201903202020032020171931", result.serverID)
        assertEquals(9, result.recordCount)
        assertEquals(1, result.deviceLines.size)

        val device = result.deviceLines[0]
        assertEquals("123400000000000", device.IMEI1)
        assertEquals("123400000000060", device.IMEI2)
        assertEquals("A5123456700000250", device.serialNumber)
        assertEquals("UNIPOS A5", device.deviceName)
    }

    @Test
    fun `test parse CSV with multiple devices`() {
        val csvContent = """
            RecordType,ServerID,IMEI1,IMEI2,SerialNumber,DeviceName,Count
            1,HDKF190320201903202020032020171931,123400000000000,123400000000060,A5123456700000250,UNIPOS A5,2
            1,HDKF190320201903202020032020171931,123400000000001,123400000000061,A5123456700000251,UNIPOS A5,2
        """.trimIndent()

        val result = csvParser.parse(csvContent)

        assertEquals(2, result.deviceLines.size)
        assertEquals("123400000000000", result.deviceLines[0].IMEI1)
        assertEquals("123400000000001", result.deviceLines[1].IMEI1)
        assertEquals(2, result.recordCount)
    }

    @Test(expected = CsvParseException::class)
    fun `test parse invalid CSV header`() {
        val csvContent = """
            WrongHeader,Invalid,Format
            1,2,3,4,5,6,7
        """.trimIndent()

        csvParser.parse(csvContent)
    }

    @Test(expected = CsvParseException::class)
    fun `test parse empty CSV`() {
        csvParser.parse("")
    }

    @Test
    fun `test parse CSV with empty lines`() {
        val csvContent = """
            RecordType,ServerID,IMEI1,IMEI2,SerialNumber,DeviceName,Count

            1,HDKF190320201903202020032020171931,123400000000000,123400000000060,A5123456700000250,UNIPOS A5,2

            1,HDKF190320201903202020032020171931,123400000000001,123400000000061,A5123456700000251,UNIPOS A5,2
        """.trimIndent()

        val result = csvParser.parse(csvContent)
        assertEquals(2, result.deviceLines.size)
        assertEquals(2, result.recordCount)
    }

    @Test(expected = CsvParseException::class)
    fun `test parse CSV with inconsistent record count`() {
        val csvContent = """
            RecordType,ServerID,IMEI1,IMEI2,SerialNumber,DeviceName,Count
            1,HDKF190320201903202020032020171931,123400000000000,123400000000060,A5123456700000250,UNIPOS A5,2
            1,HDKF190320201903202020032020171931,123400000000001,123400000000061,A5123456700000251,UNIPOS A5,3
        """.trimIndent()

        csvParser.parse(csvContent)
    }

    @Test(expected = CsvParseException::class)
    fun `test parse CSV with invalid count format`() {
        val csvContent = """
            RecordType,ServerID,IMEI1,IMEI2,SerialNumber,DeviceName,Count
            1,HDKF190320201903202020032020171931,123400000000000,123400000000060,A5123456700000250,UNIPOS A5,invalid
        """.trimIndent()

        csvParser.parse(csvContent)
    }
}
