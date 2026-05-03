package com.example.botanyhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.botanyhub.data.PlantEntity
import com.example.botanyhub.data.PlantWithWatering
import com.example.botanyhub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlantCard(
    pww: PlantWithWatering,
    onClick: () -> Unit,
    onWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plant image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GreenContainer)
            ) {
                if (pww.plant.imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = pww.plant.imageUri,
                        contentDescription = pww.plant.commonName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.align(Alignment.Center).size(36.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pww.plant.commonName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    pww.plant.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = if (pww.isDueToday) WaterBlue else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (pww.isDueToday) "Water today!"
                        else "Next: ${formatDate(pww.nextWateringDate)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (pww.isDueToday) WaterBlue else TextSecondary
                    )
                }
            }

            // Water button
            IconButton(
                onClick = onWater,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (pww.isDueToday) WaterBlue else GreenContainer)
            ) {
                Icon(
                    Icons.Rounded.WaterDrop,
                    contentDescription = "Water plant",
                    tint = if (pww.isDueToday) Color.White else GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CareInfoChip(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

fun formatDate(ts: Long): String =
    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(ts))

fun formatDateTime(ts: Long): String =
    SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(ts))