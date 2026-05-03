package com.example.botanyhub.data.repository

import android.content.Context
import android.net.Uri
import com.example.botanyhub.BuildConfig
import com.example.botanyhub.data.AppDatabase
import com.example.botanyhub.data.PlantCareTips
import com.example.botanyhub.data.PlantEntity
import com.example.botanyhub.data.PlantNetResponse
import com.example.botanyhub.data.TrefleDetailResponse
import com.example.botanyhub.data.TrefleSearchResponse
import com.example.botanyhub.data.UserProfile
import com.example.botanyhub.data.WateringLog
import com.example.botanyhub.data.api.PlantNetClient
import com.example.botanyhub.data.api.TrefleClient
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class PlantRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).plantDao()

    private val TREFLE_TOKEN = "usr-xrNv5fJP0o3Yo0C0JVGsxWqA6T3ve6_0zsPDP3upurc"

    // ── Plants ────────────────────────────────────────────────

    fun getAllPlants(): Flow<List<PlantEntity>> = dao.getAllPlants()

    suspend fun getPlantById(id: Int): PlantEntity? = dao.getPlantById(id)

    suspend fun insertPlant(plant: PlantEntity): Long = dao.insertPlant(plant)

    suspend fun updatePlant(plant: PlantEntity) = dao.updatePlant(plant)

    suspend fun deletePlant(plant: PlantEntity) = dao.deletePlant(plant)

    // ── Watering ──────────────────────────────────────────────

    fun getWateringLogs(plantId: Int): Flow<List<WateringLog>> = dao.getWateringLogs(plantId)

    suspend fun getLastWatering(plantId: Int): WateringLog? = dao.getLastWatering(plantId)

    suspend fun logWatering(plantId: Int) {
        dao.insertWateringLog(WateringLog(plantId = plantId))
    }

    // ── User Profile ──────────────────────────────────────────

    fun getUserProfile(): Flow<UserProfile?> = dao.getUserProfile()

    suspend fun upsertUserProfile(profile: UserProfile) = dao.upsertUserProfile(profile)

    suspend fun ensureDefaultProfile() {
        dao.upsertUserProfile(
            UserProfile(
                id = 1,
                name = "Vasilena",
                level = 1,
                xp = 0,
                streakDays = 0
            )
        )
    }

    // ── PlantNet Identify ─────────────────────────────────────

    suspend fun identifyPlant(imageUri: Uri): PlantNetResponse {
        val file = uriToFile(imageUri)
        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("images", file.name, requestFile)
        val organPart = "auto".toRequestBody("text/plain".toMediaType())

        return PlantNetClient.api.identifyPlant(
            apiKey = BuildConfig.PLANTNET_API_KEY,
            lang = "en",
            organ = organPart,
            images = listOf(imagePart)
        )
    }

    private fun uriToFile(uri: Uri): File {
        val file = File(context.cacheDir, "plant_scan_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    // ── Trefle Care Tips ──────────────────────────────────────

    suspend fun fetchCareTips(scientificName: String): PlantCareTips? {
        return try {
            // Взимаме само първите 2 думи (genus + species), махаме автора
            val cleanName = scientificName
                .trim()
                .split(" ")
                .take(2)
                .joinToString(" ")

            android.util.Log.d("TREFLE_RAW", "Querying Trefle with: '$cleanName' (original: '$scientificName')")

            val searchResponse: TrefleSearchResponse = TrefleClient.api.searchSpecies(
                token = TREFLE_TOKEN,
                query = cleanName  // ← cleanName вместо scientificName
            )

            android.util.Log.d("TREFLE_RAW", "Search response: $searchResponse")
            android.util.Log.d("TREFLE_RAW", "Data size: ${searchResponse.data?.size}")
            android.util.Log.d("TREFLE_RAW", "First item: ${searchResponse.data?.firstOrNull()}")

            val plantId: Int = searchResponse.data?.firstOrNull()?.id ?: run {
                android.util.Log.w("TREFLE_RAW", "No plant found for: $cleanName")
                return null
            }

            val detailResponse: TrefleDetailResponse = TrefleClient.api.getSpeciesDetail(
                id = plantId,
                token = TREFLE_TOKEN
            )

            android.util.Log.d("TREFLE_RAW", "Detail data: ${detailResponse.data}")

            val plant = detailResponse.data ?: run {
                android.util.Log.w("TREFLE_RAW", "Detail data is null for id: $plantId")
                return null
            }

            val tips = PlantCareTips.fromTreflePlant(plant)
            android.util.Log.d("TREFLE_RAW", "Parsed tips: $tips")
            tips

        } catch (e: Exception) {
            android.util.Log.e("TREFLE_RAW", "fetchCareTips failed: ${e.message}", e)
            null
        }
    }
}