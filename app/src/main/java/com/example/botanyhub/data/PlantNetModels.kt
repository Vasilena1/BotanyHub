package com.example.botanyhub.data

import com.google.gson.annotations.SerializedName

data class PlantNetResponse(
    @SerializedName("results") val results: List<PlantNetResult> = emptyList(),
    @SerializedName("remainingIdentificationRequests") val remaining: Int = 0
)

data class PlantNetResult(
    @SerializedName("score") val score: Double = 0.0,
    @SerializedName("species") val species: PlantNetSpecies = PlantNetSpecies()
)

data class PlantNetSpecies(
    @SerializedName("scientificName") val scientificName: String = "",
    @SerializedName("scientificNameWithoutAuthor") val scientificNameShort: String = "",
    @SerializedName("commonNames") val commonNames: List<String> = emptyList(),
    @SerializedName("family") val family: PlantNetFamily = PlantNetFamily(),
    @SerializedName("images") val images: List<PlantNetImage> = emptyList()
)

data class PlantNetFamily(
    @SerializedName("scientificName") val scientificName: String = "",
    @SerializedName("commonNames") val commonNames: List<String> = emptyList()
)

data class PlantNetImage(
    @SerializedName("url") val url: PlantNetImageUrl = PlantNetImageUrl()
)

data class PlantNetImageUrl(
    @SerializedName("o") val o: String = "",
    @SerializedName("m") val m: String = "",
    @SerializedName("s") val s: String = ""
)