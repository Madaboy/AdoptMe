package com.adoptme.data.api

import com.adoptme.data.model.HuachitosResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface HuachitosApiService {

    @GET("animales")
    suspend fun getAnimales(
        @Query("page") page: Int = 1
    ): HuachitosResponse
}

object HuachitosClient {
    private const val BASE_URL = "https://huachitos.cl/api/"

    val service: HuachitosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuachitosApiService::class.java)
    }
}
