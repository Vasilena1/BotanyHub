package com.example.botanyhub.data.api

import com.example.botanyhub.data.TrefleDetailResponse
import com.example.botanyhub.data.TrefleSearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TrefleApiService {

    @GET("api/v1/species/search")
    suspend fun searchSpecies(
        @Query("token") token: String,
        @Query("q") query: String
    ): TrefleSearchResponse

    @GET("api/v1/species/{id}")
    suspend fun getSpeciesDetail(
        @Path("id") id: Int,
        @Query("token") token: String
    ): TrefleDetailResponse
}

object TrefleClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: TrefleApiService = Retrofit.Builder()
        .baseUrl("https://trefle.io/")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TrefleApiService::class.java)
}