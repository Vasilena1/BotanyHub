package com.example.botanyhub.data.api

import com.example.botanyhub.data.PlantNetResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface PlantNetApi {
    @Multipart
    @POST("v2/identify/all")
    suspend fun identifyPlant(
        @Query("api-key") apiKey: String,
        @Query("lang") lang: String = "en",
        @Query("include-related-images") includeImages: Boolean = false,
        @Part("organs") organ: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): PlantNetResponse
}

object PlantNetClient {
    val api: PlantNetApi = retrofit2.Retrofit.Builder()
        .baseUrl("https://my-api.plantnet.org/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .client(
            okhttp3.OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val req = chain.request()
                    android.util.Log.d("PlantNet_URL", "→ ${req.url}")
                    val response = chain.proceed(req)
                    android.util.Log.d("PlantNet_URL", "← ${response.code}")
                    response
                }
                .build()
        )
        .build()
        .create(PlantNetApi::class.java)
}