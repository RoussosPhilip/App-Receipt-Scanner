package com.devphilip.receipt_scanner.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import android.net.Uri

@Composable
fun rememberDocumentScanner(
    onScanSuccess: (List<Uri>) -> Unit,
    onScanCancelled: () -> Unit = {},
    onScanError: (Exception) -> Unit = {}
): () -> Unit {
    val context = LocalContext.current

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)  // Allow gallery import
            .setPageLimit(1)  // Only 1 page (receipt)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                scanningResult?.pages?.let { pages ->
                    val uris = pages.mapNotNull { it.imageUri }
                    if (uris.isNotEmpty()) {
                        onScanSuccess(uris)
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                onScanCancelled()
            }
        }
    }

    return remember {
        {
            scanner.getStartScanIntent(context as Activity)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }
                .addOnFailureListener { exception ->
                    onScanError(exception)
                }
        }
    }
}
