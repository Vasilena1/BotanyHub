package com.example.botanyhub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.botanyhub.data.PlantCareTips
import com.example.botanyhub.data.PlantEntity
import com.example.botanyhub.data.PlantNetResult
import com.example.botanyhub.data.repository.PlantRepository
import com.example.botanyhub.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    result: PlantNetResult,
    imageUri: String,
    careTips: PlantCareTips? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { PlantRepository(context) }
    val scope = rememberCoroutineScope()

    val commonName = result.species.commonNames?.firstOrNull() ?: result.species.scientificName ?: ""
    var name by remember { mutableStateOf(commonName) }
    var isIndoor by remember { mutableStateOf(true) }
    var sunlight by remember { mutableStateOf("medium") }
    var waterDays by remember { mutableStateOf("7") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // ── Autofill from care tips ───────────────────────────────
    LaunchedEffect(careTips) {
        careTips?.let { tips ->
            // Watering — директно от числото
            waterDays = tips.wateringIntervalDays.toString()

            // Indoor/Outdoor — от Trefle
            isIndoor = tips.isIndoorPlant

            // Sunlight
            sunlight = when {
                tips.sunlightTips.contains("Full sun", ignoreCase = true) -> "high"
                tips.sunlightTips.contains("Partial", ignoreCase = true) -> "medium"
                tips.sunlightTips.contains("Low light", ignoreCase = true) -> "low"
                tips.sunlightTips.contains("Deep shade", ignoreCase = true) -> "low"
                else -> "medium"
            }

            // Notes — autofill with all tips
            notes = buildString {
                appendLine(tips.wateringTips)
                appendLine(tips.sunlightTips)
                appendLine(tips.humidityTips)
                appendLine(tips.temperatureTips)
                appendLine(tips.soilTips)
                tips.generalTips.forEach { appendLine(it) }
            }.trim()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add to My Garden", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenSurface)
            )
        },
        containerColor = GreenSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Care tips banner ──────────────────────────────
            if (careTips != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = GreenPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Fields auto-filled from care data ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Plant name ────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Rounded.Spa, null, tint = GreenPrimary) }
            )

            // ── Scientific name (read-only) ───────────────────
            OutlinedTextField(
                value = result.species.scientificName ?: "",
                onValueChange = {},
                label = { Text("Scientific name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = TextSecondary
                )
            )

            // ── Indoor / Outdoor ──────────────────────────────
            Text(
                "Location",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LocationChip("🏠 Indoor", isIndoor) { isIndoor = true }
                LocationChip("🌳 Outdoor", !isIndoor) { isIndoor = false }
            }

            // ── Sunlight ──────────────────────────────────────
            Text(
                "Sunlight",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("low", "medium", "high").forEach { level ->
                    FilterChip(
                        selected = sunlight == level,
                        onClick = { sunlight = level },
                        label = { Text(level.replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenContainer,
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
            }

            // ── Watering ──────────────────────────────────────
            OutlinedTextField(
                value = waterDays,
                onValueChange = { waterDays = it.filter { c -> c.isDigit() } },
                label = { Text("Water every (days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Rounded.WaterDrop, null, tint = WaterBlue) },
                supportingText = {
                    if (careTips != null) {
                        Text(
                            "Suggested by Trefle based on soil humidity data",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                }
            )

            // ── Notes ─────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Care notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 8
            )

            // ── Save button ───────────────────────────────────
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        repo.insertPlant(
                            PlantEntity(
                                commonName = name.ifBlank { commonName },
                                scientificName = result.species.scientificName ?: "",
                                family = result.species.family?.scientificName ?: "",
                                imageUri = imageUri,
                                plantNetImageUrl = result.species.images?.firstOrNull()?.url?.m ?: "",
                                isIndoor = isIndoor,
                                sunlight = sunlight,
                                waterFrequencyDays = waterDays.toIntOrNull() ?: 7,
                                notes = notes
                            )
                        )
                        onSaved()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to My Garden", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun LocationChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GreenContainer,
            selectedLabelColor = GreenPrimary
        )
    )
}