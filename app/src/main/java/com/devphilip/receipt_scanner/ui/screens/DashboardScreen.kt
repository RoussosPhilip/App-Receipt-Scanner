package com.devphilip.receipt_scanner.ui.screens

import androidx.compose.foundation.layout.*
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
import com.devphilip.receipt_scanner.ui.components.AddButton
import com.devphilip.receipt_scanner.ui.components.AddReceiptBottomSheet
import com.devphilip.receipt_scanner.ui.components.CustomMenuButton
import com.devphilip.receipt_scanner.ui.components.FuelRecordItem
import com.devphilip.receipt_scanner.ui.components.SummaryCard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.devphilip.receipt_scanner.data.AppDataBase
import com.devphilip.receipt_scanner.ui.components.StatisticsCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.format
import androidx.compose.foundation.lazy.LazyRow
import com.devphilip.receipt_scanner.ui.components.DashboardStat


@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToManualEntry: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit  ={},
    onNavigateToRecordDetail: (Int) -> Unit ={}
) {

    val context = LocalContext.current
    val database = remember { AppDataBase.getDataBase(context) }
    val dao = database.fuelRecordDao()

    val records by dao.getAllRecords().collectAsState(initial = emptyList())


    var showBottomSheet by remember { mutableStateOf(false) }
    val totalAmount = records.sumOf { it.amount }
    val totalLiters = records.sumOf { it.liters }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val today = remember { LocalDate.now() }
    val monthAgo = remember { today.minusDays(30) }

    val lastMonthRecords = remember(records) {
        records.filter { record ->
            try {
                val d = LocalDate.parse(record.date, formatter)
                !d.isBefore(monthAgo) && !d.isAfter(today)
            } catch (e: Exception) {
                false
            }
        }
    }

    val lastMonthLiters = lastMonthRecords.sumOf { it.liters }
    val lastMonthAmount = lastMonthRecords.sumOf { it.amount }

    val avgPriceOverall =
        if (totalLiters > 0.0) totalAmount / totalLiters else 0.0

    val stats = listOf(
        DashboardStat(
            title = "Liters (30 days)",
            value = String.format("%.1f L", lastMonthLiters),
//            subtitle = "Last 30 days"
        ),
        DashboardStat(
            title = "Cost (30 days)",
            value = String.format("€%.2f", lastMonthAmount),
//            subtitle = "Last 30 days"
        ),
        DashboardStat(
            title = "Avg price (Overall)",
            value = if (avgPriceOverall > 0.0)
                String.format("€%.3f/L", avgPriceOverall)
            else
                "—",
//            subtitle = "Overall"
        )
    )

    val scope = rememberCoroutineScope()

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
                    .padding(12.dp)
            ) {
//                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CustomMenuButton(onClick = {
                        // Will do something
                    })

                    //For testing
//                    Button(onClick = {
//                        scope.launch {
//                            dao.deleteAll()
//                            println("All records deleted")
//                        }
//                    }) {
//                        Text("Clear All")
//                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SummaryCard(
                    totalAmount = totalAmount,
                    totalLiters = totalLiters
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                )  {
                    items(stats){ stat ->
                        StatisticsCard(stat = stat)
                    }
                }

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

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val itemHeight = 80.dp + 8.dp
                    val availableHeight = maxHeight
                    val itemsToShow = (availableHeight / itemHeight).toInt()

                    Column(modifier = Modifier.fillMaxWidth()) {
                        records.take(itemsToShow).forEach { record ->
                            FuelRecordItem(
                                record = record,
                                onClick = {
                                    onNavigateToRecordDetail(record.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    AddReceiptBottomSheet(
        showSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false },
        onScanClick = {
            showBottomSheet = false
            onNavigateToScanner()
            println("Open Camera!")
        },
        onManualClick = {
            showBottomSheet = false
            onNavigateToManualEntry()
            println("Open Manual Entry!")
        }
    )
}

