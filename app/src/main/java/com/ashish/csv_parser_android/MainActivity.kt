package com.ashish.csv_parser_android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ashish.csv_parser_android.model.DeviceLine
import com.ashish.csv_parser_android.model.OutputFormat
import com.ashish.csv_parser_android.model.ParseState
import com.ashish.csv_parser_android.ui.theme.CsvParserAndroidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CsvParserAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val viewModel: MainViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var selectedOutputFormat by remember { mutableStateOf(OutputFormat.JSON) }
    val context = LocalContext.current

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processFile(it, context.contentResolver, selectedOutputFormat)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CSV Parser") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Output Format:", style = MaterialTheme.typography.bodyLarge)
                Row {
                    OutputFormat.entries.forEach { format ->
                        FilterChip(
                            selected = format == selectedOutputFormat,
                            onClick = {
                                selectedOutputFormat = format
                                viewModel.regenerateOutput(format)
                            },
                            label = { Text(format.name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            // File Selection Button
            FilledTonalButton(
                onClick = { filePickerLauncher.launch("text/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Select CSV File")
            }

            // Content Area
            when (val state = uiState) {
                ParseState.Idle -> {
                    Text(
                        "Select a CSV file to begin parsing",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                ParseState.Loading -> {
                    CircularProgressIndicator()
                }
                is ParseState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ParseState.Success -> {
                    SuccessContent(state)
                }
            }
        }
    }
}

@Composable
fun SuccessContent(state: ParseState.Success) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // Device Report Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Server ID: ${state.deviceReport.serverID}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Record Count: ${state.deviceReport.recordCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Output Format Display
        Text(
            "Output (${state.outputFormat}):",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                state.outputString,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

        // Device List
        Text(
            "Parsed Devices:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(state.deviceReport.deviceLines) { device ->
                DeviceCard(device)
            }
        }
    }
}

@Composable
fun DeviceCard(device: DeviceLine) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Device: ${device.deviceName}", style = MaterialTheme.typography.titleSmall)
            Text("IMEI 1: ${device.IMEI1}")
            Text("IMEI 2: ${device.IMEI2}")
            Text("Serial: ${device.serialNumber}")
        }
    }
}