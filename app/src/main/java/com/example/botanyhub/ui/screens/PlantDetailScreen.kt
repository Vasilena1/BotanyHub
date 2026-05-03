package com.example.botanyhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.botanyhub.ui.components.*
import com.example.botanyhub.ui.theme.*
import com.example.botanyhub.viewmodel.PlantDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Int,
    onBack: () -> Unit,
    vm: PlantDetailViewModel = viewModel()
) {
    LaunchedEffect(plantId) { vm.loadPlant(plantId) }

    val plant by vm.plant.collectAsState()
    val logs by vm.wateringLogs.collectAsState()

    plant?.let { p ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(p.commonName, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenSurface),
                    actions = {
                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (p.isIndoor) GreenContainer else SunYellow.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (p.isIndoor) "🏠 Indoor" else "☀️ Outdoor",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (p.isIndoor) GreenPrimary else TempOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                )
            },
            containerColor = GreenSurface
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(GreenContainer)
                    ) {
                        val imgSrc = p.imageUri.ifEmpty { p.plantNetImageUrl }
                        if (imgSrc.isNotEmpty()) {
                            AsyncImage(
                                model = imgSrc,
                                contentDescription = p.commonName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Rounded.Spa, null, tint = GreenPrimary, modifier = Modifier.align(Alignment.Center).size(80.dp))
                        }
                    }
                }

                // Scientific name
                item {
                    Text(p.scientificName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    if (p.family.isNotEmpty())
                        Text("Family: ${p.family}", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }

                // Care info chips
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CareInfoChip(
                                Icons.Rounded.LightMode,
                                "Sunlight",
                                p.sunlight.replaceFirstChar { it.uppercase() },
                                SunYellow
                            )
                            CareInfoChip(
                                Icons.Rounded.WaterDrop,
                                "Water",
                                "Every ${p.waterFrequencyDays}d",
                                WaterBlue
                            )
                            CareInfoChip(
                                Icons.Rounded.Thermostat,
                                "Location",
                                if (p.isIndoor) "Indoor" else "Outdoor",
                                TempOrange
                            )
                        }
                    }
                }

                // Water now button
                item {
                    Button(
                        onClick = { vm.waterNow() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaterBlue)
                    ) {
                        Icon(Icons.Rounded.WaterDrop, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Water Now", style = MaterialTheme.typography.titleMedium)
                    }
                }

                // Watering history
                item {
                    Text("Watering History", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                if (logs.isEmpty()) {
                    item {
                        Text("No watering logged yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                } else {
                    items(logs) { log ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp, 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(WaterBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Rounded.WaterDrop, null, tint = WaterBlue, modifier = Modifier.size(18.dp)) }
                                Spacer(Modifier.width(12.dp))
                                Text("Watered on ${formatDateTime(log.wateredAt)}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                    }
                }

                // Notes
                if (p.notes.isNotEmpty()) {
                    item {
                        Text("Notes", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenContainer)
                        ) {
                            Text(p.notes, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }
    } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(color = GreenPrimary)
    }
}