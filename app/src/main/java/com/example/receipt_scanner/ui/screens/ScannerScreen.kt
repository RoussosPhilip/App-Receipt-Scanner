package com.example.receipt_scanner.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.receipt_scanner.ui.components.ChevronLeft
import com.example.receipt_scanner.ui.components.rememberDocumentScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onScanComplete: (Uri) -> Unit
) {
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val launchScanner = rememberDocumentScanner(
        onScanSuccess = { uris ->
            if (uris.isNotEmpty()) {
                scannedImageUri = uris.first()
            }
        },
        onScanCancelled = {
            onNavigateBack()  // Go back if cancelled
        },
        onScanError = { exception ->
            errorMessage = exception.message ?: "Unknown error"
            showError = true
        }
    )

    LaunchedEffect(Unit) {
        launchScanner()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (scannedImageUri == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Opening camera...",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChevronLeft(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(45.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Receipt Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Image preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            AsyncImage(
                                model = scannedImageUri,
                                contentDescription = "Scanned receipt",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scannedImageUri = null
                                launchScanner()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Retake")
                        }

                        Button(
                            onClick = {
                                scannedImageUri?.let { uri ->
                                    onScanComplete(uri)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Confirm")
                        }
                    }
                }
            }

            if (showError) {
                AlertDialog(
                    onDismissRequest = {
                        showError = false
                        onNavigateBack()
                    },
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = {
                            showError = false
                            onNavigateBack()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}
