package com.devphilip.receipt_scanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.devphilip.receipt_scanner.ui.components.DashboardStat


@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToManualEntry: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit  ={},
    onNavigateToStatistics: () -> Unit ={},
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
    val today = LocalDate.now()

    val startOfMonth = today.withDayOfMonth(1)

    val firstDayOfLastMonth = today
        .minusMonths(1)
        .withDayOfMonth(1)

    val lastDayOfLastMonth = today
        .withDayOfMonth(1)
        .minusDays(1)


    val thisMonthRecords = remember(records){
        records.filter {record ->
            try {
                val d = LocalDate.parse(record.date, formatter)
                !d.isBefore(startOfMonth) && !d.isAfter(today)
            } catch (e: Exception) {
                false
            }
        }
    }

    val thisMonthLiters = thisMonthRecords.sumOf { it.liters }
    val thisMonthAmount = thisMonthRecords.sumOf { it.amount }
    val thisMonthFillUps = thisMonthRecords.size


    val lastMonthRecords = remember(records) {
        records.filter { record ->
            try {
                val d = LocalDate.parse(record.date, formatter)
                !d.isBefore(firstDayOfLastMonth) &&
                        !d.isAfter(lastDayOfLastMonth)
            } catch (e: Exception) {
                false
            }
        }
    }

    val lastMonthFillUps = lastMonthRecords.size
    val lastMonthAmount = lastMonthRecords.sumOf { it.amount }
    val lastMonthLiters = lastMonthRecords.sumOf { it.liters }


    val avgPriceOverall =
        if (totalLiters > 0.0) totalAmount / totalLiters else 0.0

    val thisMonthPrices = thisMonthRecords
        .filter { it.liters > 0.0 }
        .map { it.amount / it.liters }

    val minPriceThisMonth = thisMonthPrices.minOrNull() ?: 0.0
    val maxPriceThisMonth = thisMonthPrices.maxOrNull() ?: 0.0

    val mostUsedFuel = records
        .groupBy { it.fuelType }
        .maxByOrNull { it.value.size }
        ?.key ?: "—"



    val stats = listOf(
        DashboardStat(
            title = "Min / Max (This Month)",
            value = if (minPriceThisMonth > 0.0 && maxPriceThisMonth > 0.0)
                "€%.3f↓\n€%.3f↑".format(minPriceThisMonth, maxPriceThisMonth)
            else "—"
        ),
        DashboardStat(
            title = "Avg price (Overall)",
            value = if (avgPriceOverall > 0.0)
                String.format("€%.3f/L", avgPriceOverall)
            else
                "—",
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
                }

                Spacer(modifier = Modifier.height(20.dp))

                SummaryCard(
                    thisMonthAmount = thisMonthAmount,
                    thisMonthLiters = thisMonthLiters,
                    thisMonthFillUps = thisMonthFillUps,
                    lastMonthAmount = lastMonthAmount,
                    lastMonthLiters = lastMonthLiters,
                    lastMonthFillUps = lastMonthFillUps
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FilledTonalButton(
                        onClick = {
                            println("View More clicked!")
                            onNavigateToStatistics()
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
                            text = "View More",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    stats.forEach { stat ->
                        StatisticsCard(
                            stat = stat,
                            modifier = Modifier.weight(1f)
                        )
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
                            println("View More clicked!")
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
                            text = "View More",
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

