package com.ashish.csv_parser_android

import android.content.ContentResolver
import android.net.Uri
import app.cash.turbine.test
import com.ashish.csv_parser_android.model.DeviceLine
import com.ashish.csv_parser_android.model.DeviceReport
import com.ashish.csv_parser_android.model.OutputFormat
import com.ashish.csv_parser_android.model.ParseState
import com.ashish.csv_parser_android.parser.CsvParser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private lateinit var viewModel: MainViewModel
    private lateinit var csvParser: CsvParser
    private val testDispatcher = StandardTestDispatcher()

    private val sampleDeviceReport = DeviceReport(
        serverID = "test123",
        recordCount = 1,
        deviceLines = listOf(
            DeviceLine(
                deviceName = "Test Device",
                IMEI1 = "123",
                IMEI2 = "456",
                serialNumber = "789"
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        csvParser = mockk(relaxed = true)
        viewModel = MainViewModel(csvParser)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `test initial state is idle`() = runTest {
        viewModel.uiState.test {
            assertEquals(ParseState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test successful file processing with JSON output`() = runTest {
        // Mock dependencies
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()
        val csvContent = "sample csv content"

        // Setup mocks
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(csvContent.toByteArray())
        coEvery { csvParser.parse(csvContent) } returns sampleDeviceReport

        // Test state flow
        viewModel.uiState.test {
            assertEquals(ParseState.Idle, awaitItem())

            viewModel.processFile(uri, contentResolver, OutputFormat.JSON)
            assertEquals(ParseState.Loading, awaitItem())

            val successState = awaitItem() as ParseState.Success
            assertEquals(sampleDeviceReport, successState.deviceReport)
            assertTrue(successState.outputString.contains("test123"))
            assertEquals(OutputFormat.JSON, successState.outputFormat)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test format regeneration`() = runTest {
        // Mock initial state
        coEvery { csvParser.parse(any()) } returns sampleDeviceReport

        viewModel.uiState.test {
            // Skip initial idle state
            skipItems(1)

            // Set initial success state with JSON
            val contentResolver = mockk<ContentResolver>()
            val uri = mockk<Uri>()
            every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream("test".toByteArray())

            viewModel.processFile(uri, contentResolver, OutputFormat.JSON)
            skipItems(1) // Skip Loading state

            val jsonState = awaitItem() as ParseState.Success
            assertEquals(OutputFormat.JSON, jsonState.outputFormat)

            // format changed to XML
            viewModel.regenerateOutput(OutputFormat.XML)
            val xmlState = awaitItem() as ParseState.Success
            assertEquals(OutputFormat.XML, xmlState.outputFormat)
            assertTrue(xmlState.outputString.contains("<DeviceReport"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test error handling for invalid file`() = runTest {
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()

        every { contentResolver.openInputStream(uri) } throws Exception("Invalid file")

        viewModel.uiState.test {
            assertEquals(ParseState.Idle, awaitItem())

            viewModel.processFile(uri, contentResolver, OutputFormat.JSON)
            assertEquals(ParseState.Loading, awaitItem())

            val errorState = awaitItem() as ParseState.Error
            assertTrue(errorState.message.contains("Invalid file"))

            cancelAndIgnoreRemainingEvents()
        }
    }
}
