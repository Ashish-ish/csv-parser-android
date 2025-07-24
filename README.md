<img width="1080" height="2424" alt="Screenshot_20250724_223125" src="https://github.com/user-attachments/assets/b87096ca-990d-4d2b-b873-361ca1245cf1" /># CSV Parser Android

A modern Android application that parses CSV files and converts them into structured JSON or XML output, built with Jetpack Compose and following MVVM architecture.

## What is it?

CSV Parser Android is a utility application that:
- Reads CSV files containing device information
- Parses the data into a structured format
- Provides real-time conversion between JSON and XML output formats
- Displays both the formatted output and a visual representation of the parsed data

## Why was it built?

The application addresses several common needs:
1. Converting CSV data into more developer-friendly formats (JSON/XML)
2. Providing a mobile-first approach to data format conversion
3. Demonstrating modern Android development practices
4. Implementing a clean architecture with clear separation of concerns

## Working Screenshots

<img width="1080" height="2424" alt="Screenshot_20250724_223021" src="https://github.com/user-attachments/assets/201c3324-1caa-4520-a337-91c43cb17891" />
<img width="1080" height="2424" alt="Screenshot_20250724_223044" src="https://github.com/user-attachments/assets/f6d7328b-ecb3-49db-b365-326ddd7dc120" />
<img width="1080" height="2424" alt="Screenshot_20250724_223125" src="https://github.com/user-attachments/assets/c5402468-5cb2-490d-aae9-16de6b854897" />
<img width="1080" height="2424" alt="Screenshot_20250724_223134" src="https://github.com/user-attachments/assets/ac1d5371-0a32-49cc-9850-785a09f7c4b4" />
<img width="1080" height="2424" alt="Screenshot_20250724_223150" src="https://github.com/user-attachments/assets/efedffb1-d496-465c-ae0a-6712343f26c3" />


## Technical Implementation

### Architecture
- **MVVM (Model-View-ViewModel)** pattern for clean separation of concerns
- **Unidirectional Data Flow** for predictable state management
- **Repository Pattern** for data operations

### Key Technologies
1. **Jetpack Compose** for modern UI development
2. **Kotlin Coroutines** for asynchronous operations
3. **Kotlin Serialization** for JSON/XML conversion
4. **Storage Access Framework** for safe file handling
5. **StateFlow** for reactive state management

### Features
- File selection using Android's Storage Access Framework
- Real-time format switching between JSON and XML
- Visual representation of parsed data
- Error handling and loading states
- Modern Material 3 design

### Data Flow
1. User selects a CSV file through the system file picker
2. ViewModel processes the file using CsvParser
3. Data is converted to DeviceReport model
4. Output is generated in selected format (JSON/XML)
5. UI updates to display both formatted output and visual representation

## How to Use

1. Launch the application
2. Click "Select CSV File" button
3. Choose a CSV file using the system file picker
4. View the parsed data in the default format (JSON)
5. Toggle between JSON and XML formats using the format chips
6. Scroll through the visual representation of parsed devices

## Project Structure

```
app/
├── model/
│   ├── DeviceReport.kt
│   ├── DeviceLine.kt
│   ├── OutputFormat.kt
│   └── ParseState.kt
├── parser/
│   └── CsvParser.kt
├── ui/
│   └── theme/
├── MainActivity.kt
└── MainViewModel.kt
```

## Requirements
- Android 5.0 (API level 21) or higher
- Compatible CSV file with device information

## Implementation Details

### State Management
The app uses `ParseState` sealed class for handling different UI states:
- `Idle`: Initial state
- `Loading`: File processing state
- `Success`: Successful parsing with data
- `Error`: Error state with message

### File Processing
- Uses ContentResolver for safe file access
- Implements coroutines for background processing
- Handles large files efficiently

### Data Conversion
- Implements custom CSV parser
- Uses Kotlin Serialization for JSON/XML conversion
- Maintains data integrity during format switching

