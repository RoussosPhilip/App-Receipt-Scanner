package com.example.receipt_scanner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.receipt_scanner.ui.components.DatePickerDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onNavigateBack: () -> Unit,
    onSaveReceipt: (String, String, Double, Double) -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var typeOfFuel by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val dateInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = stationName,
                onValueChange = { stationName = it },
                label = { Text("Station Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                interactionSource = dateInteractionSource,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            OutlinedTextField(
                value = liters,
                onValueChange = { liters = it },
                label = { Text("Liters") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Total Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val litersDouble = liters.toDoubleOrNull() ?: 0.0
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0

                    onSaveReceipt(stationName, date, litersDouble, amountDouble)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = stationName.isNotBlank() && liters.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Save")
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