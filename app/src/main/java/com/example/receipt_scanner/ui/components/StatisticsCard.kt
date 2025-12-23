package com.example.receipt_scanner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DashboardStat(
    val title: String,
    val value: String,
    val subtitle: String? = null
)

@Composable
fun StatisticsCard(
    stat: DashboardStat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(width = 160.dp, height = 110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 10.dp))
        ) {
            Text(
                text = stat.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.background,
            )

            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background
                    ),
                modifier = Modifier.padding(top = 6.dp)
            )

            stat.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
