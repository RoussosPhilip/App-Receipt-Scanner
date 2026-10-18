package com.devphilip.receipt_scanner.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devphilip.receipt_scanner.ui.components.ChevronLeft
import com.devphilip.receipt_scanner.ui.components.FuelRecordItem
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.devphilip.receipt_scanner.data.AppDataBase


@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToRecordDetail: (Int) -> Unit,
){
    val context = LocalContext.current
    val database = remember { AppDataBase.getDataBase(context) }
    val dao = database.fuelRecordDao()
    val records by dao.getAllRecords().collectAsState(initial = emptyList())

    val groupedRecords = remember(records) {
        records.groupBy { record ->
            try {
                val parts = record.date.split("-")
                val year = parts[0]
                val month = when(parts[1]) {
                    "01" -> "Jan"
                    "02" -> "Feb"
                    "03" -> "Mar"
                    "04" -> "Apr"
                    "05" -> "May"
                    "06" -> "Jun"
                    "07" -> "Jul"
                    "08" -> "Aug"
                    "09" -> "Sep"
                    "10" -> "Oct"
                    "11" -> "Nov"
                    "12" -> "Dec"
                    else -> parts[1]
                }
                "$month $year"  // "Dec 2025"
            } catch (e: Exception) {
                "Unknown"
            }
        }
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
                    ChevronLeft(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(45.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedRecords.forEach { (month, recordsInMonth) ->
                        item {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        items(recordsInMonth) { record ->
                            FuelRecordItem(
                                record = record,
                                onClick = {
                                    onNavigateToRecordDetail(record.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

//                        item {
//                            HorizontalDivider(
//                                modifier = Modifier.padding(vertical = 16.dp),
//                                thickness = 1.dp,
//                                color = MaterialTheme.colorScheme.outlineVariant
//                            )
//                        }
                    }
                }


            }


        }
    }

}