package com.example.botanyhub.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.botanyhub.data.PlantCareTips
import com.example.botanyhub.data.PlantNetResult
import com.example.botanyhub.ui.theme.*
import com.example.botanyhub.viewmodel.CareTipsState
import com.example.botanyhub.viewmodel.ScanUiState
import com.example.botanyhub.viewmodel.ScanViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateToAdd: (PlantNetResult, String, PlantCareTips?) -> Unit,
    onBack: () -> Unit,
    vm: ScanViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val careTipsState by vm.careTipsState.collectAsState()
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok) photoUri?.let { vm.identify(it) } }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { vm.identify(it) } }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
            file.parentFile?.mkdirs()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            photoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
            file.parentFile?.mkdirs()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan a Plant", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { vm.reset(); onBack() }) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenSurface)
            )
        },
        containerColor = GreenSurface
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is ScanUiState.Idle -> IdleScanContent(
                    onCamera = { launchCamera() },
                    onGallery = { galleryLauncher.launch("image/*") }
                )
                is ScanUiState.Loading -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                    Text("Identifying plant...", color = TextSecondary)
                }
                is ScanUiState.Error -> ErrorContent(s.message) { vm.reset() }
                is ScanUiState.Success -> ResultsContent(
                    state = s,
                    careTipsState = careTipsState,
                    onAddPlant = { result ->
                        vm.fetchCareTipsAndAdd(result) { tips ->
                            onNavigateToAdd(result, s.imageUri.toString(), tips)
                        }
                    },
                    onRetry = { vm.reset() }
                )
            }
        }
    }
}

@Composable
private fun IdleScanContent(onCamera: () -> Unit, onGallery: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(GreenContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CameraAlt, null, tint = GreenPrimary, modifier = Modifier.size(72.dp))
        }
        Text(
            "Identify any plant instantly",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Take a photo or pick from gallery to identify and save your plant",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onCamera,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Icon(Icons.Rounded.CameraAlt, null)
            Spacer(Modifier.width(8.dp))
            Text("Take Photo", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = onGallery,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Photo, null)
            Spacer(Modifier.width(8.dp))
            Text("Choose from Gallery", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ResultsContent(
    state: ScanUiState.Success,
    careTipsState: CareTipsState,
    onAddPlant: (PlantNetResult) -> Unit,
    onRetry: () -> Unit
) {
    val isLoadingTips = careTipsState is CareTipsState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = state.imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Text(
            "Best Matches",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        state.results.forEachIndexed { index, result ->
            val name = result.species.commonNames?.firstOrNull()
                ?: result.species.scientificName
                ?: "Unknown"
            val confidence = (result.score * 100).toInt()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index == 0) GreenContainer else Color.White
                ),
                onClick = { if (!isLoadingTips) onAddPlant(result) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (index == 0) {
                            Text("Best match", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                        }
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(result.species.scientificName ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("$confidence% confidence", style = MaterialTheme.typography.labelLarge, color = GreenPrimary)
                    }
                    if (isLoadingTips && index == 0) {
                        CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.ArrowForwardIos, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ── Loading tips indicator ──────────────────────────────
        if (isLoadingTips) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GreenContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Fetching care tips...", style = MaterialTheme.typography.bodySmall, color = GreenPrimary)
                }
            }
        }

// ── Care Tips Card ──────────────────────────────────────
        if (careTipsState is CareTipsState.Ready) {
            val tips = (careTipsState as CareTipsState.Ready).tips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GreenContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🌿 Care Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                    Text(tips.sunlightTips, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(tips.wateringTips, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(tips.humidityTips, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(tips.temperatureTips, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Text(tips.soilTips, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    if (tips.generalTips.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        tips.generalTips.forEach { tip ->
                            Text(tip, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }

// ── Not found ───────────────────────────────────────────
        if (careTipsState is CareTipsState.NotFound) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    "⚠️ No care tips found for this species.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        TextButton(
            onClick = onRetry,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoadingTips
        ) {
            Text("Scan again", color = GreenPrimary)
        }
    }
}

@Composable
private fun ErrorContent(msg: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text("Could not identify plant", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(msg, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try Again") }
    }
}