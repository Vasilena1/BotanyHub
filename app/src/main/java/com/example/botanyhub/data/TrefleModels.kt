package com.example.botanyhub.data

data class TrefleSearchResponse(
    val data: List<TreflePlantItem>?
)

data class TreflePlantItem(
    val id: Int,
    val common_name: String?,
    val scientific_name: String?,
    val image_url: String?
)

data class TrefleDetailResponse(
    val data: TreflePlant?
)

data class TreflePlant(
    val id: Int?,
    val common_name: String?,
    val scientific_name: String?,
    val image_url: String?,
    val family_common_name: String?,
    val growth: TrefleGrowth?,
    val specifications: TrefleSpecifications?
)

data class TrefleGrowth(
    val light: Int?,
    val atmospheric_humidity: Int?,
    val minimum_precipitation: TrefleMeasure?,
    val maximum_precipitation: TrefleMeasure?,
    val minimum_temperature: TrefleTemp?,
    val maximum_temperature: TrefleTemp?,
    val soil_nutriments: Int?,
    val soil_humidity: Int?,
    val ph_minimum: Double?,
    val ph_maximum: Double?,
    val days_to_harvest: Int?,
    val row_spacing: TrefleMeasure?,
    val spread: TrefleMeasure?
)

data class TrefleSpecifications(
    val ligneous_type: String?,
    val growth_form: String?,
    val growth_habit: String?,
    val growth_rate: String?,
    val average_height: TrefleMeasure?,
    val maximum_height: TrefleMeasure?,
    val nitrogen_fixation: String?,
    val shape_and_orientation: String?,
    val toxicity: String?
)

data class TrefleMeasure(
    val cm: Double?,
    val mm: Double?,
    val minimum: TrefleMeasureValue?,
    val maximum: TrefleMeasureValue?
)

data class TrefleMeasureValue(
    val mm: Double?,
    val cm: Double?
)

data class TrefleTemp(
    val deg_c: Double?,
    val deg_f: Double?
)

// ─── Parsed care tips for UI ──────────────────────────────────
data class PlantCareTips(
    val wateringTips: String,
    val sunlightTips: String,
    val humidityTips: String,
    val temperatureTips: String,
    val soilTips: String,
    val generalTips: List<String>,
    val toxicity: String?,
    val growthRate: String?,
    val plantType: String?,
    val wateringIntervalDays: Int,
    val isIndoorPlant: Boolean
) {
    companion object {
        fun fromTreflePlant(plant: TreflePlant): PlantCareTips {
            val g = plant.growth
            val s = plant.specifications

            val watering = when {
                g?.soil_humidity != null -> when {
                    g.soil_humidity >= 7 -> "💧 Frequent watering needed — keep soil consistently moist"
                    g.soil_humidity >= 4 -> "💧 Water moderately — allow top soil to dry between waterings"
                    else -> "💧 Minimal watering — drought tolerant, let soil dry completely"
                }
                else -> "💧 Water when the top 2–3 cm of soil feels dry"
            }

            val sunlight = when {
                g?.light != null -> when {
                    g.light >= 8 -> "☀️ Full sun — needs 6+ hours of direct sunlight daily"
                    g.light >= 5 -> "🌤️ Partial sun — 3–6 hours of indirect bright light"
                    g.light >= 3 -> "🌥️ Low light — thrives in indirect or filtered light"
                    else -> "🌑 Deep shade tolerant — keep away from direct sun"
                }
                else -> "☀️ Place in bright indirect light for best results"
            }

            val humidity = when {
                g?.atmospheric_humidity != null -> when {
                    g.atmospheric_humidity >= 7 -> "💦 High humidity needed (60%+) — mist regularly or use a humidifier"
                    g.atmospheric_humidity >= 4 -> "💦 Average humidity (40–60%) — standard indoor conditions are fine"
                    else -> "💦 Low humidity tolerant — no misting required"
                }
                else -> "💦 Average indoor humidity is sufficient"
            }

            val temp = when {
                g?.minimum_temperature?.deg_c != null && g.maximum_temperature?.deg_c != null ->
                    "🌡️ Ideal temperature: ${g.minimum_temperature.deg_c.toInt()}°C – ${g.maximum_temperature.deg_c.toInt()}°C"
                g?.minimum_temperature?.deg_c != null ->
                    "🌡️ Keep above ${g.minimum_temperature.deg_c.toInt()}°C — protect from frost"
                else -> "🌡️ Keep at normal room temperature (18–24°C)"
            }

            val soil = when {
                g?.ph_minimum != null && g.ph_maximum != null ->
                    "🪴 Prefers soil pH ${g.ph_minimum}–${g.ph_maximum}. " +
                            if ((g.soil_nutriments ?: 5) >= 6) "Rich, fertile soil recommended."
                            else "Well-draining soil recommended."
                else -> "🪴 Use well-draining potting mix"
            }

            val tips = mutableListOf<String>()
            s?.growth_rate?.let {
                tips.add("🌱 Growth rate: ${it.replaceFirstChar { c -> c.uppercase() }}")
            }
            s?.toxicity?.takeIf { it != "none" && it.isNotBlank() }?.let {
                tips.add("⚠️ Toxicity: $it — keep away from pets and children")
            }
            g?.days_to_harvest?.let {
                tips.add("📅 Days to harvest: $it days")
            }
            s?.ligneous_type?.let {
                tips.add("🌿 Plant type: ${it.replaceFirstChar { c -> c.uppercase() }}")
            }
            s?.average_height?.cm?.let {
                tips.add("📏 Average height: ${it.toInt()} cm")
            }

            val wateringDays = when {
                g?.soil_humidity != null -> when {
                    g.soil_humidity >= 8 -> 2
                    g.soil_humidity >= 6 -> 4
                    g.soil_humidity >= 4 -> 7
                    g.soil_humidity >= 2 -> 12
                    else -> 18
                }
                else -> 7
            }

            val indoorPlant = when {
                g?.light != null && g.light <= 4 -> true
                g?.light != null && g.light >= 8 -> false
                s?.ligneous_type?.lowercase() in setOf("herb", "forb") -> true
                s?.ligneous_type?.lowercase() in setOf("tree", "shrub", "liana") -> false
                else -> true
            }

            return PlantCareTips(
                wateringTips = watering,
                sunlightTips = sunlight,
                humidityTips = humidity,
                temperatureTips = temp,
                soilTips = soil,
                generalTips = tips,
                toxicity = s?.toxicity,
                growthRate = s?.growth_rate,
                plantType = s?.ligneous_type,
                wateringIntervalDays = wateringDays,
                isIndoorPlant = indoorPlant
            )
        }
    }
}