package com.devphilip.receipt_scanner.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devphilip.receipt_scanner.data.AppDataBase
import com.devphilip.receipt_scanner.ui.components.DatePickerDialog
import com.devphilip.receipt_scanner.ui.components.TimePickerDialog
import com.devphilip.receipt_scanner.ui.components.ChevronLeft
import androidx.compose.runtime.rememberCoroutineScope
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.devphilip.receipt_scanner.data.remote.getGasStationLogo
import com.devphilip.receipt_scanner.data.remote.loadBitmapFromAssets
import com.devphilip.receipt_scanner.service.FirebaseReceiptService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.devphilip.receipt_scanner.data.StationsDataBase
import com.devphilip.receipt_scanner.data.StringMatcher
import com.google.firebase.firestore.FirebaseFirestore

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
    onSaveReceipt: (String, String, Double, Double) -> Unit,
    onNavigateToFavoritesStation: () -> Unit,
    onNavigateToMap: () -> Unit,
    selectedStationName: String? = null,
    selectedStationBrand: String? = null,
    selectedFuelType: String? = null,
    selectedUnitPrice: String? = null,
    scannedImageUri: String? = null,
    editRecordId: Int? = null
) {
    val calendar = Calendar.getInstance()
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)

    var stationName by remember { mutableStateOf(selectedStationName ?: "") }
    var stationBrand by remember { mutableStateOf(selectedStationBrand ?: "") }
    var date by remember { mutableStateOf(currentDate) }
    var time by remember { mutableStateOf(currentTime) }
    var liters by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf(selectedFuelType ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val androidId = remember {
        android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    val database = remember { AppDataBase.getDataBase(context) }
    val dao = database.fuelRecordDao()
    val scope = rememberCoroutineScope()

    val dateInteractionSource = remember { MutableInteractionSource() }
    val timeInteractionSource = remember { MutableInteractionSource() }

    var lastEdited by remember { mutableStateOf(EditedField.NONE) }

    var isAnalyzing by remember { mutableStateOf(false) }
    val receiptService = remember { FirebaseReceiptService(context) }

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

    val logoBitmap = remember(stationBrand) {
        val fileName = getGasStationLogo(stationBrand)
        loadBitmapFromAssets(context, fileName)
    }

    val isEditMode = editRecordId != null

    val existingRecord by remember(editRecordId) {
        if (editRecordId != null)
            dao.getRecordById(editRecordId)
        else
            kotlinx.coroutines.flow.flowOf(null)
    }.collectAsState(initial = null)

    LaunchedEffect(existingRecord) {
        existingRecord?.let { r ->
            stationName  = r.stationName
            stationBrand = r.stationBrand
            date         = r.date
            time         = r.time
            liters       = r.liters.toString()
            amount       = r.amount.toString()
            unitPrice    = r.unitPrice.toString()
            fuelType     = r.fuelType
        }
    }

    val hasStationData = stationName.isNotBlank() || stationBrand.isNotBlank()

    LaunchedEffect(selectedStationName, selectedFuelType, selectedUnitPrice) {
        selectedStationName?.let  { stationName  = it }
        selectedStationBrand?.let { stationBrand = it }
        selectedFuelType?.let     { fuelType     = it }
        selectedUnitPrice?.let {
            if (it.isNotBlank()) {
                unitPrice  = it
                lastEdited = EditedField.UNIT_PRICE
            }
        }
    }

    LaunchedEffect(scannedImageUri) {
        scannedImageUri?.let { uriString ->
            isAnalyzing = true
            receiptService.analyzeReceipt(Uri.parse(uriString))
                .onSuccess { data ->
                    if (data.stationName.isNotBlank()) stationName = data.stationName
                    if (data.stationBrand.isNotBlank()) stationBrand = data.stationBrand
                    if (data.fuelType.isNotBlank()) fuelType = data.fuelType
                    if (data.date.isNotBlank()) date = data.date
                    if (data.time.isNotBlank()) time = data.time
                    if (data.liters > 0) liters = data.liters.toString()
                    if (data.unitPrice > 0) unitPrice = data.unitPrice.toString()
                    if (data.amount > 0) amount = data.amount.toString()

                    val stationsDao = StationsDataBase.getDataBase(context).stationsDao()

                    val matchedBrand = if (data.stationBrand.isNotBlank()) {
                        val brandNames = stationsDao.getAllBrandNames()
                        StringMatcher.findBestMatch(data.stationBrand, brandNames)
                    } else null

                    if (matchedBrand != null) {
                        stationBrand = matchedBrand
                    }

                    if (data.stationName.isNotBlank()) {
                        val ownerCandidates = if (matchedBrand != null) {
                            stationsDao.getOwnersByBrand(matchedBrand)
                        } else {
                            stationsDao.getAllOwnerNames()
                        }

                        StringMatcher.findBestMatch(data.stationName, ownerCandidates)
                            ?.let { stationName = it }
                    }
                }
                .onFailure { }
            isAnalyzing = false
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
                    .padding(12.dp)
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
                        text = "Receipt Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Select Gas Station",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp)
                )

                Spacer(Modifier.height(9.dp))

                if (!hasStationData) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedCard(
                            onClick = { onNavigateToMap()},
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Map",
                                    modifier = Modifier.size(35.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Choose on map",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        OutlinedCard(
                            onClick = { onNavigateToFavoritesStation() },
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(35.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Choose from favorites",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = stationBrand,
                                    onValueChange = { stationBrand = it },
                                    label = { Text("Brand") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(15.dp),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = stationName,
                                onValueChange = { stationName = it },
                                label = { Text("Station") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(15.dp),
                                singleLine = true
                            )
                        }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "More Information",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                )

//                Spacer(Modifier.height(9.dp))
//
//                OutlinedTextField(
//                    value = odometer,
//                    onValueChange = { odometer = it },
//                    label = {Text("Odometer")},
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(15.dp)
//                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15.dp)
                    )

                    OutlinedTextField(
                        value = time,
                        onValueChange = {},
                        label = {Text("Time")},
                        readOnly = true,
                        interactionSource = timeInteractionSource,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(15.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fuelType,
                    onValueChange = { fuelType = it },
                    label = { Text("Fuel") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    singleLine = true
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

                        val record = com.devphilip.receipt_scanner.data.FuelRecord(
                            id           = editRecordId ?: 0,
                            stationName  = stationName,
                            stationBrand = stationBrand,
                            date         = date,
                            time         = time,
                            liters       = litersDouble,
                            unitPrice    = parseNumber(unitPrice) ?: 0.0,
                            amount       = amountDouble,
                            fuelType     = fuelType
                        )

                        scope.launch {
                            dao.insert(record)
                            println("Saved to database: $record")
                        }

                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = stationName.isNotBlank() &&
                            stationBrand.isNotBlank() &&
                            date.isNotBlank() &&
                            time.isNotBlank() &&
                            liters.isNotBlank() &&
                            amount.isNotBlank()
                ) {
                    Text(if (editRecordId != null) "Save Changes" else "Save")
                }
            }
        }

        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(13.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Analyzing receipt...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
