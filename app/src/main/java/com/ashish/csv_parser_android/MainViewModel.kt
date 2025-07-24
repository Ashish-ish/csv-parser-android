package com.ashish.csv_parser_android

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashish.csv_parser_android.model.DeviceReport
import com.ashish.csv_parser_android.model.EvoluteDeviceManagement
import com.ashish.csv_parser_android.model.OutputFormat
import com.ashish.csv_parser_android.model.ParseState
import com.ashish.csv_parser_android.parser.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import java.io.IOException

class MainViewModel : ViewModel() {
    private val csvParser = CsvParser()
    private val _uiState = MutableStateFlow<ParseState>(ParseState.Idle)
    val uiState: StateFlow<ParseState> = _uiState.asStateFlow()

    private val prettyJson = Json { prettyPrint = true }
    private val prettyXml = XML { indent = 4 }

    fun processFile(uri: Uri, contentResolver: android.content.ContentResolver, outputFormat: OutputFormat) {
        viewModelScope.launch {
            _uiState.value = ParseState.Loading
            try {
                val content = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    } ?: throw IOException("Could not read file")
                }

                val deviceReport = withContext(Dispatchers.Default) {
                    csvParser.parse(content)
                }

                val outputString = withContext(Dispatchers.Default) {
                    generateOutput(deviceReport, outputFormat)
                }

                _uiState.value = ParseState.Success(
                    deviceReport = deviceReport,
                    outputString = outputString,
                    outputFormat = outputFormat
                )
            } catch (e: Exception) {
                _uiState.value = ParseState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun generateOutput(deviceReport: DeviceReport, outputFormat: OutputFormat): String {
        val evoluteDeviceManagement = EvoluteDeviceManagement(listOf(deviceReport))
        return when (outputFormat) {
            OutputFormat.JSON -> prettyJson.encodeToString(evoluteDeviceManagement)
            OutputFormat.XML -> prettyXml.encodeToString(evoluteDeviceManagement)
        }
    }

    fun regenerateOutput(newFormat: OutputFormat) {
        val currentState = _uiState.value
        if (currentState is ParseState.Success) {
            val newOutput = generateOutput(currentState.deviceReport, newFormat)
            _uiState.value = ParseState.Success(
                deviceReport = currentState.deviceReport,
                outputString = newOutput,
                outputFormat = newFormat
            )
        }
    }
}
