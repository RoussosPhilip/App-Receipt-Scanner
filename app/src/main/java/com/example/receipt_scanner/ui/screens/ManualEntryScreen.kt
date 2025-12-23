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
import com.example.receipt_scanner.ui.components.TimePickerDialog
import com.example.receipt_scanner.ui.components.ChevronLeft
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

enum class EditedField { NONE, LITERS, AMOUNT, UNIT_PRICE }

private fun parseNumber(input: String): Double?{
    if (input.isEmpty()) return null
    val normalized = input.replace(",",".")
    return normalized.toDoubleOrNull()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onSaveReceipt: (String, String, Double, Double) -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = remember { AppDataBase.getDataBase(context) }
    val dao = database.fuelRecordDao()
    val scope = rememberCoroutineScope()

    val dateInteractionSource = remember { MutableInteractionSource() }
    val timeInteractionSource = remember { MutableInteractionSource() }

    var lastEdited by remember { mutableStateOf(EditedField.NONE) }


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

    LaunchedEffect(timeInteractionSource) {
        timeInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release){
                showTimePicker = true
            }
        }
    }

    LaunchedEffect(liters, amount, unitPrice, lastEdited) {
        val litersVal = parseNumber(liters)
        val amountVal = parseNumber(amount)
        val unitVal = parseNumber(unitPrice)

        when (lastEdited) {
            EditedField.LITERS -> {
                if (litersVal != null && litersVal > 0 && amountVal != null) {
                    unitPrice = String.format("%.3f", amountVal / litersVal)
                } else if (litersVal != null && litersVal > 0 && unitVal != null) {
                    amount = String.format("%.2f", unitVal * litersVal)
                }
            }
            EditedField.AMOUNT -> {
                if (amountVal != null && unitVal != null && unitVal > 0) {
                    liters = String.format("%.3f", amountVal / unitVal)
                } else if (litersVal != null && litersVal > 0 && amountVal != null) {
                    unitPrice = String.format("%.3f", amountVal / litersVal)
                }
            }
            EditedField.UNIT_PRICE -> {
                if (amountVal != null && unitVal != null && unitVal > 0) {
                    liters = String.format("%.3f", amountVal / unitVal)
                } else if (litersVal != null && litersVal > 0 && unitVal != null) {
                    amount = String.format("%.2f", unitVal * litersVal)
                }
            }
            EditedField.NONE -> Unit
        }

        if (liters.isBlank() || amount.isBlank()) {
            if (lastEdited != EditedField.UNIT_PRICE) {
                unitPrice = ""
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
                    value = time,
                    onValueChange = {},
                    label = {Text("Time")},
                    readOnly = true,
                    interactionSource = timeInteractionSource,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        lastEdited = EditedField.AMOUNT
                    },
                    label = { Text("Total Amount (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = {
                        unitPrice = it
                        lastEdited = EditedField.UNIT_PRICE
                    },
                    label = { Text("Price per Liter (€/L)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = liters,
                    onValueChange = {
                        liters = it
                        lastEdited = EditedField.LITERS
                    },
                    label = { Text("Liters") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val litersDouble = parseNumber(liters) ?: 0.0
                        val amountDouble = parseNumber(amount) ?: 0.0

                        val record = com.example.receipt_scanner.data.FuelRecord(
                            stationName = stationName,
                            date = date,
                            time = time,
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
                            time.isNotBlank() &&
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

    if (showTimePicker) {
        TimePickerDialog(
            onConfirm = { selectedTime ->
                time = selectedTime
                showTimePicker = false
            },
            onDismiss = {showTimePicker = false}
        )
    }
}
