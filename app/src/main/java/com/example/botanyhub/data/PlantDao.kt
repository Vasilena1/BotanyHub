package com.example.botanyhub.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Query("SELECT * FROM plants ORDER BY addedDate DESC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: Int): PlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getPlantCount(): Int

    @Query("SELECT * FROM watering_logs WHERE plantId = :plantId ORDER BY wateredAt DESC")
    fun getWateringLogs(plantId: Int): Flow<List<WateringLog>>

    @Query("SELECT * FROM watering_logs WHERE plantId = :plantId ORDER BY wateredAt DESC LIMIT 1")
    suspend fun getLastWatering(plantId: Int): WateringLog?

    @Insert
    suspend fun insertWateringLog(log: WateringLog)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(profile: UserProfile)
}