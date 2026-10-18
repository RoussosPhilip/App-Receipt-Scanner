package com.devphilip.receipt_scanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devphilip.receipt_scanner.data.AppDataBase
import com.devphilip.receipt_scanner.data.FuelRecord
import com.devphilip.receipt_scanner.ui.components.ChevronLeft
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    val context  = LocalContext.current
    val dao      = remember { AppDataBase.getDataBase(context).fuelRecordDao() }
    val records  by dao.getAllRecords().collectAsState(initial = emptyList())

    val formatter    = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val today        = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)

    val sortedRecords = remember(records) {
        records
            .filter { it.unitPrice > 0.0 }
            .sortedBy {
                try { LocalDate.parse(it.date, formatter) }
                catch (e: Exception) { LocalDate.MIN }
            }
    }

    val totalAmount     = records.sumOf { it.amount }
    val totalLiters     = records.sumOf { it.liters }
    val avgPriceOverall = if (totalLiters > 0.0) totalAmount / totalLiters else 0.0

    val thisMonthPrices = remember(records) {
        records
            .filter { record ->
                try {
                    val d = LocalDate.parse(record.date, formatter)
                    !d.isBefore(startOfMonth) && !d.isAfter(today)
                } catch (e: Exception) { false }
            }
            .filter { it.liters > 0.0 }
            .map { it.amount / it.liters }
    }

    val minPriceThisMonth = thisMonthPrices.minOrNull() ?: 0.0
    val maxPriceThisMonth = thisMonthPrices.maxOrNull() ?: 0.0

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
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
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChevronLeft(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Your Statistics",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (sortedRecords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No fill-ups recorded yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    StatSummaryRow(
                        avgPriceOverall   = avgPriceOverall,
                        minPriceThisMonth = minPriceThisMonth,
                        maxPriceThisMonth = maxPriceThisMonth
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    PriceHistoryChart(records = sortedRecords)
                    Spacer(modifier = Modifier.height(24.dp))
                    CostPerMonthChart(records = records, formatter = formatter)
                }
            }
        }
    }
}

@Composable
private fun StatSummaryRow(
    avgPriceOverall: Double,
    minPriceThisMonth: Double,
    maxPriceThisMonth: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "MIN / MAX THIS MONTH (€/L)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (minPriceThisMonth > 0.0 && maxPriceThisMonth > 0.0) {
                Text(
                    text = "↓ €%.3f  ↑ €%.3f".format(minPriceThisMonth, maxPriceThisMonth),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "AVG OVERALL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (avgPriceOverall > 0.0) "€%.3f/L".format(avgPriceOverall) else "—",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun PriceHistoryChart(records: List<FuelRecord>) {
    val modelProducer  = remember { CartesianChartModelProducer() }
    val chartColor     = MaterialTheme.colorScheme.primary
    val fullFormatter  = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val labelFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yy") }

    val xFormatter = CartesianValueFormatter { _, x, _ ->
        val idx = x.toInt()
        try {
            LocalDate.parse(records[idx].date, fullFormatter).format(labelFormatter)
        } catch (e: Exception) { "" }
    }

    val minY = remember(records) {
        records.minOfOrNull { it.unitPrice }?.minus(0.05) ?: 0.0
    }
    val maxY = remember(records) {
        records.maxOfOrNull { it.unitPrice }?.plus(0.05) ?: 2.0
    }

    val rangeProvider = remember(minY, maxY) {
        CartesianLayerRangeProvider.fixed(minY = minY, maxY = maxY)
    }

    val line = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(chartColor))
    )

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End
    )

    LaunchedEffect(records) {
        modelProducer.runTransaction {
            lineSeries { series(records.map { it.unitPrice }) }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Price per Liter (€/L)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Per fill-up",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider  = LineCartesianLayer.LineProvider.series(line),
                        rangeProvider = rangeProvider
                    ),
                    startAxis  = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter       = xFormatter,
                        labelRotationDegrees = 45f
                    )
                ),
                modelProducer = modelProducer,
                scrollState   = scrollState, 
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun CostPerMonthChart(
    records: List<FuelRecord>,
    formatter: DateTimeFormatter
) {
    val monthlyData = remember(records) {
        records
            .groupBy {
                try {
                    val d = LocalDate.parse(it.date, formatter)
                    "${d.year}-${d.monthValue.toString().padStart(2, '0')}"
                } catch (e: Exception) { "Unknown" }
            }
            .filter { it.key != "Unknown" }
            .toSortedMap()
    }

    val months        = remember(monthlyData) { monthlyData.keys.toList() }
    val modelProducer = remember { CartesianChartModelProducer() }
    val chartColor    = MaterialTheme.colorScheme.primary

    val labelFormatter = remember { DateTimeFormatter.ofPattern("MMM yy") }

    val xFormatter = CartesianValueFormatter { _, x, _ ->
        val idx = x.toInt()
        try {
            val yearMonth = months.getOrNull(idx) ?: return@CartesianValueFormatter ""
            val date = LocalDate.of(
                yearMonth.take(4).toInt(),
                yearMonth.takeLast(2).toInt(),
                1
            )
            date.format(labelFormatter)
        } catch (e: Exception) { "" }
    }

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End
    )

    LaunchedEffect(monthlyData) {
        modelProducer.runTransaction {
            columnSeries {
                series(monthlyData.values.map { it.sumOf { r -> r.amount } })
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Monthly Cost (€)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Total spend per month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                fill  = fill(chartColor),
                                shape = CorneredShape.rounded(
                                    topLeftPercent  = 25,
                                    topRightPercent = 25
                                )
                            )
                        )
                    ),
                    startAxis  = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter       = xFormatter,
                        labelRotationDegrees = 45f
                    )
                ),
                modelProducer = modelProducer,
                scrollState   = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}