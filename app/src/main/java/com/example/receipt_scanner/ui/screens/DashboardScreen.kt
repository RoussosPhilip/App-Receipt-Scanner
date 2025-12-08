package com.example.receipt_scanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receipt_scanner.data.FuelRecord
import com.example.receipt_scanner.ui.components.AddButton
import com.example.receipt_scanner.ui.components.AddReceiptBottomSheet
import com.example.receipt_scanner.ui.components.CustomMenuButton
import com.example.receipt_scanner.ui.components.FuelRecordItem
import com.example.receipt_scanner.ui.components.SummaryCard

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToManualEntry: () -> Unit = {},
    onNavigateToHistory: () -> Unit  ={}
) {
    // Demo data (in the future will come from ViewModel)
    val records = listOf(
        FuelRecord("Shell - Kifisias", "27 Nov", 20.0, 39.5),
        FuelRecord("BP - Attica Road", "23 Nov", 35.0, 70.0),
        FuelRecord("Eko - Piraeus", "18 Nov", 15.0, 28.2)
    )

    var showBottomSheet by remember { mutableStateOf(false) }
    val totalAmount = records.sumOf { it.amount }
    val totalLiters = records.sumOf { it.liters }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AddButton(
                onClick = {
                    showBottomSheet = true
                    println("Add receipt clicked!")
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
//                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomMenuButton(onClick = {
                        // Will do something
                    })

                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SummaryCard(
                    totalAmount = totalAmount,
                    totalLiters = totalLiters
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                    )

                    FilledTonalButton(
                        onClick = {
                            println("View All clicked!")
                            onNavigateToHistory()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 10.dp,
                            vertical = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .defaultMinSize(minHeight = 1.dp)
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(records) { record ->
                        FuelRecordItem(record = record)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    AddReceiptBottomSheet(
        showSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false },
        onScanClick = {
            println("Open Camera!")
        },
        onManualClick = {
            showBottomSheet = false
            onNavigateToManualEntry()
            println("Open Manual Entry!")
        }
    )
}

