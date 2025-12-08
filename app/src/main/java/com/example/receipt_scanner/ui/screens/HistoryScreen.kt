package com.example.receipt_scanner.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receipt_scanner.data.FuelRecord
import com.example.receipt_scanner.ui.components.ChevronLeft
import com.example.receipt_scanner.ui.components.FuelRecordItem

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    records: List<FuelRecord>
){

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
                    items(records) { record ->
                        FuelRecordItem(record = record)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

            }


        }
    }

}