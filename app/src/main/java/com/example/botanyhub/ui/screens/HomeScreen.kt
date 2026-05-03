package com.example.botanyhub.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botanyhub.ui.components.PlantCard
import com.example.botanyhub.ui.theme.*
import com.example.botanyhub.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToScan: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val plants by vm.plantsWithWatering.collectAsState()
    val profile by vm.profile.collectAsState()

    Scaffold(
        containerColor = GreenSurface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = GreenPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.CameraAlt, "Scan plant", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hello, ${profile?.name ?: "Gardener"} 🌿",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${plants.count { it.isDueToday }} plant(s) need watering today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Lv${profile?.level ?: 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stats strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        "My Plants", plants.size.toString(),
                        Icons.Rounded.Spa, GreenPrimary, Modifier.weight(1f)
                    )
                    StatCard(
                        "Streak", "${profile?.streakDays ?: 0}d 🔥",
                        Icons.Rounded.Whatshot, TempOrange, Modifier.weight(1f)
                    )
                    StatCard(
                        "XP", "${profile?.xp ?: 0}",
                        Icons.Rounded.Star, SunYellow, Modifier.weight(1f)
                    )
                }
            }

            // Section title
            item {
                Text(
                    "Your Garden",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Plants list
            if (plants.isEmpty()) {
                item { EmptyGardenCard(onScan = onNavigateToScan) }
            } else {
                items(plants, key = { it.plant.id }) { pww ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                vm.deletePlant(pww.plant)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                    Color(0xFFE53935) else Color.Transparent,
                                label = "delete_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 2.dp)
                                    .background(color, RoundedCornerShape(20.dp))
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        PlantCard(
                            pww = pww,
                            onClick = { onNavigateToDetail(pww.plant.id) },
                            onWater = { vm.waterPlant(pww.plant.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun EmptyGardenCard(onScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GreenContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Spa, null,
                tint = GreenPrimary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No plants yet!",
                style = MaterialTheme.typography.titleMedium,
                color = GreenPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Scan a flower to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onScan,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Icon(Icons.Rounded.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Scan a Plant")
            }
        }
    }
}