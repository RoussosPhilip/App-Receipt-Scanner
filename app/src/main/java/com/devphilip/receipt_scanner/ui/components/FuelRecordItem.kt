package com.devphilip.receipt_scanner.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devphilip.receipt_scanner.data.FuelRecord
import com.devphilip.receipt_scanner.data.remote.getGasStationLogo
import com.devphilip.receipt_scanner.data.remote.loadBitmapFromAssets
import com.mapbox.maps.extension.style.expressions.dsl.generated.color
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FuelRecordItem(
    record: FuelRecord,
    onClick: () -> Unit = {}
) {

    val displayDate = remember(record.date) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(record.date)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            record.date
        }
    }

    val shape = RoundedCornerShape(16.dp)

    val context = LocalContext.current

    val logoBitmap = remember(record.stationBrand) {
        val fileName = getGasStationLogo(record.stationBrand)
        loadBitmapFromAssets(context, fileName)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(shape)
            .clickable { onClick() },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color(0x2FDADADA)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center

            ) {
                logoBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = record.stationBrand,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.stationBrand,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

//                Text(
//                    text = record.stationName,
//                    style = MaterialTheme.typography.labelMedium,
//                    fontWeight = FontWeight.Bold
//                )

                Text(
                    text = "$displayDate • ${record.time} • ${record.liters} L",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = String.format("%.2f €", record.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
