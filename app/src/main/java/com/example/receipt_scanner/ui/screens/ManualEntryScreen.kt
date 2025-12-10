package com.example.receipt_scanner.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receipt_scanner.data.AppDataBase
import com.example.receipt_scanner.ui.components.DatePickerDialog
import com.example.receipt_scanner.ui.components.ChevronLeft
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onSaveReceipt: (String, String, Double, Double) -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = remember { AppDataBase.getDataBase(context) }
    val dao = database.fuelRecordDao()
    val scope = rememberCoroutineScope()

    val dateInteractionSource = remember { MutableInteractionSource() }

    val displayDate = remember(date) {
        if (date.isNotEmpty()) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate!!)
            } catch (e: Exception) {
                date
            }
        } else {
            ""
        }
    }


    LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                        text = "Receipt Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Station Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = displayDate,
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    interactionSource = dateInteractionSource,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = liters,
                    onValueChange = { liters = it },
                    label = { Text("Liters") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val litersDouble = liters.toDoubleOrNull() ?: 0.0
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0

                        val record = com.example.receipt_scanner.data.FuelRecord(
                            stationName = stationName,
                            date = date,
                            liters = litersDouble,
                            amount = amountDouble
                        )

                        scope.launch {
                            dao.insert(record)
                            println("Saved to database: $record")
                        }

                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = stationName.isNotBlank() &&
                            date.isNotBlank() &&
                            liters.isNotBlank() &&
                            amount.isNotBlank()
                ) {
                    Text("Save")
                }

            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                date = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
